package io.github.djfx229.rangpur.feature.player.domain.interactor

import io.github.djfx229.rangpur.common.domain.Logger
import io.github.djfx229.rangpur.feature.player.domain.controller.PlayerController
import io.github.djfx229.rangpur.feature.player.domain.model.state.MetadataState
import io.github.djfx229.rangpur.feature.player.domain.model.state.PlaybackState
import io.github.djfx229.rangpur.feature.radio.domain.model.StreamMetadata
import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.common.domain.di.getConfigRepository
import io.github.djfx229.rangpur.common.domain.interactor.AudioInteractor
import io.github.djfx229.rangpur.common.domain.repository.ConfigRepository
import io.github.djfx229.rangpur.core.data.Audio
import io.github.djfx229.rangpur.core.data.AudioInPlaylist
import io.github.djfx229.rangpur.feature.player.domain.model.PlayerSource
import io.github.djfx229.rangpur.core.feature.player.domain.model.*
import io.github.djfx229.rangpur.feature.radio.domain.model.RadioStation
import io.github.djfx229.rangpur.feature.player.domain.model.PlayerCommand
import io.github.djfx229.rangpur.feature.player.domain.model.PlayerConfig
import io.github.djfx229.rangpur.feature.player.domain.model.PlayerPosition
import io.github.djfx229.rangpur.feature.player.domain.model.PlayerRepeatMode
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.lang.Double.min
import kotlin.math.max


class PlayerInteractor(
    private val di: DependencyInjector,
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {

    interface Listener {
        fun onChangePosition(info: PlayerPosition)
        fun onChangeState(state: PlaybackState) {}
        fun onChangeMetadata(state: MetadataState) {}
        fun onChangeCurrentIndex(index: Int, item: Any) {}
        fun onChangeRepeatMode(mode: PlayerRepeatMode)
        fun onChangeShuffleMode(isShuffleMode: Boolean)
    }

    private val log: Logger by lazy { di.get() }

    private val player by lazy {
        di.get(PlayerController::class)
    }

    private val audioInteractor by lazy {
        di.get(AudioInteractor::class)
    }

    private val configRepository: ConfigRepository<PlayerConfig> by lazy {
        di.getConfigRepository()
    }

    private var playlist: List<Any>? = null
    private var randomQueue: List<Int>? = null
    private var mapToRealIndex: Map<Int, Int>? = null

    private var currentIndex: Int = -1
    private var currentPlaybackState: PlaybackState = PlaybackState.Stopped
    private var currentMetadataState: MetadataState = MetadataState.EmptyMetadataState
    private var externalPlaybackListeners = emptyList<Listener>().toMutableList()
    private var repeatMode: PlayerRepeatMode = PlayerRepeatMode.NONE
    private var isShuffleMode = false
    private val handleCommandMutex = Mutex()

    private var job = SupervisorJob()
    private var playbackScope = CoroutineScope(Dispatchers.Default + job)

    private val playerListener = object : PlayerController.Listener {
        override fun onPlay() {
            currentItem?.let {
                setPlaybackState(PlaybackState.Playing)
            }
        }

        override fun onPause() {
            currentItem?.let {
                setPlaybackState(PlaybackState.Paused)
            }
        }

        override fun onChangeStreamMetadata(metadata: StreamMetadata) {
            currentItem?.let { item ->
                if (item is RadioStation) {
                    setMetadataState(
                        MetadataState.Stream(item, metadata)
                    )
                }
            }
        }
    }

    var currentItem: Any? = null
        private set

    init {
        ioScope.launch(Dispatchers.IO) {
            configRepository.load()
            withContext(Dispatchers.Main) {
                val config = configRepository.get()
                repeatMode = config.repeatMode
                isShuffleMode = config.isShuffleMode
                externalPlaybackListeners.forEach { listener ->
                    listener.onChangeShuffleMode(isShuffleMode)
                    listener.onChangeRepeatMode(repeatMode)
                }
            }
        }
    }

    fun addListener(listener: Listener) {
        externalPlaybackListeners.add(listener)
        listener.onChangeShuffleMode(isShuffleMode)
        listener.onChangeRepeatMode(repeatMode)
        listener.onChangeState(currentPlaybackState)
        listener.onChangeMetadata(currentMetadataState)
    }

    fun removeListener(listener: Listener) {
        externalPlaybackListeners.remove(listener)
    }

    fun getPlayerPosition(): PlayerPosition = player.getPosition()

    private fun tryChangeIndexAndPlayAudio(requestIndex: Int) {
        val size = playlist?.size ?: return

        val index = if (requestIndex < 0) {
            if (repeatMode == PlayerRepeatMode.PLAYLIST) {
                size - 1
            } else {
                return
            }
        } else if (requestIndex >= size) {
            if (repeatMode == PlayerRepeatMode.PLAYLIST) {
                0
            } else {
                return
            }
        } else {
            requestIndex
        }

        val newIndex = shuffleNewIndexIfNeed(index)
        currentItem = playlist?.getOrNull(newIndex) ?: return
        currentIndex = index
        tryPlayCurrentItem()
    }

    private fun tryPlayCurrentItem() {
        val item = currentItem ?: return

        externalPlaybackListeners.forEach { listener ->
            listener.onChangeCurrentIndex(shuffleNewIndexIfNeed(currentIndex), item)
        }

        val source = when (item) {
            is Audio -> {
                setMetadataState(MetadataState.AudioItem(item))
                PlayerSource.File(audioInteractor.getFullPath(item))
            }

            is AudioInPlaylist -> {
                val audio = item.audio ?: return
                setMetadataState(MetadataState.AudioItem(audio))
                PlayerSource.File(audioInteractor.getFullPath(audio))
            }

            is File -> {
                setMetadataState(MetadataState.File(item.name))
                PlayerSource.File(item.absolutePath)
            }

            is RadioStation -> {
                setMetadataState(MetadataState.Stream(item))
                PlayerSource.Stream(item.streamUrl)
            }

            else -> PlayerSource.Unsupported
        }
        player.open(source)
        player.play()
        setPlaybackState(PlaybackState.Playing)
        startTimer()
    }

    private fun setPlaybackState(state: PlaybackState) {
        externalPlaybackListeners.forEach { listener ->
            listener.onChangeState(state)
        }
        currentPlaybackState = state
    }

    private fun setMetadataState(state: MetadataState) {
        externalPlaybackListeners.forEach { listener ->
            listener.onChangeMetadata(state)
        }
        currentMetadataState = state
    }

    suspend fun handleCommand(command: PlayerCommand) {
        log.d(this, "handleCommand(command=${command.javaClass}) waiting mutex")
        handleCommandMutex.withLock {
            log.d(this, "handleCommand(command=${command.javaClass}) start handling")
            when (command) {
                is PlayerCommand.Open<*> -> handleCommandOpen(command)
                PlayerCommand.Play -> handleCommandPlay()
                PlayerCommand.Pause -> handleCommandPause()
                PlayerCommand.Stop -> handleCommandStop()

                PlayerCommand.Next -> handleCommandNext()
                PlayerCommand.Previous -> handleCommandPrevious()

                is PlayerCommand.SeekTo -> handleCommandSeekTo(command)
                is PlayerCommand.RelativeSeek -> handleCommandRelativeSeek(command)
                is PlayerCommand.BeatsSeek -> handleCommandBeatsSeek(command)

                PlayerCommand.Release -> handleCommandRelease()

                PlayerCommand.ToggleRepeatMode -> handleCommandToggleRepeatMode()
                PlayerCommand.ToggleShuffleMode -> handleCommandToggleShuffleMode()
            }
        }
    }

    private fun handleCommandOpen(command: PlayerCommand.Open<*>) {
        this.currentItem = command.currentItem
        if (playlist != command.items) {
            playlist = command.items
            if (isShuffleMode) {
                generateShuffleData()
            }
        }
        currentIndex = getRealPlaylistIndex(command.index)

        player.setListener(playerListener)
        tryPlayCurrentItem()
    }

    private fun handleCommandPlay() {
        if (player.state == PlaybackState.Paused) {
            player.play()
        } else {
            tryPlayCurrentItem()
        }
    }

    private fun handleCommandPause() {
        if (player.state == PlaybackState.Paused) {
            player.play()
        } else {
            player.pause()
        }
    }

    private fun handleCommandStop() {
        player.stop()
        stopTimer()
        setPlaybackState(PlaybackState.Stopped)
    }

    private fun handleCommandNext() {
        tryChangeIndexAndPlayAudio(currentIndex + 1)
    }

    private fun handleCommandPrevious() {
        tryChangeIndexAndPlayAudio(currentIndex - 1)
    }

    private fun handleCommandSeekTo(command: PlayerCommand.SeekTo) {
        if (currentItem is Audio || currentItem is AudioInPlaylist || currentItem is File) {
            player.seekTo(command.positionSeconds)
        }
    }

    private fun handleCommandRelativeSeek(command: PlayerCommand.RelativeSeek) {
        val positionInfo = player.getPosition()
        val newPosition = if (command.relativePositionSeconds > 0) {
            min(
                positionInfo.position + command.relativePositionSeconds,
                positionInfo.duration,
            )
        } else {
            max(
                positionInfo.position + command.relativePositionSeconds,
                0.0,
            )
        }
        handleCommandSeekTo(PlayerCommand.SeekTo(newPosition))
    }

    private fun handleCommandBeatsSeek(command: PlayerCommand.BeatsSeek) {
        if (command.beats == 0) return
        val item = currentItem
        val bpm = when (item) {
            is Audio -> item.bpm
            is AudioInPlaylist -> item.audio?.bpm
            else -> null
        } ?: return
        val beatsToSeconds = 1.0 / command.beats * bpm
        handleCommandRelativeSeek(PlayerCommand.RelativeSeek(beatsToSeconds))
    }

    private fun handleCommandRelease() {
        stopTimer()
        player.release()
    }

    private fun handleCommandToggleRepeatMode() {
        val newMode = when (repeatMode) {
            PlayerRepeatMode.NONE -> PlayerRepeatMode.PLAYLIST
            PlayerRepeatMode.PLAYLIST -> PlayerRepeatMode.ONE_TRACK
            PlayerRepeatMode.ONE_TRACK -> PlayerRepeatMode.NONE
        }
        changeRepeatMode(newMode)
    }

    private fun handleCommandToggleShuffleMode() {
        isShuffleMode = !isShuffleMode
        log.d(this, "change shuffle mode to $isShuffleMode")

        if (isShuffleMode) {
            generateShuffleData()
            currentIndex = 0
        } else {
            currentIndex = randomQueue.orEmpty().getOrNull(currentIndex) ?: 0
            releaseShuffleData()
        }

        saveConfig()
        externalPlaybackListeners.forEach { listener ->
            listener.onChangeShuffleMode(isShuffleMode)
        }
    }

    private fun startTimer() {
        stopTimer()
        job = SupervisorJob()
        playbackScope = CoroutineScope(Dispatchers.Default + job)
        playbackScope.launch {
            try {
                while (isActive) {
                    delay(100)
                    if (player.isPlayerReady()) {
                        val positionInfo = player.getPosition()

                        if (player.state == PlaybackState.Playing) {
                            externalPlaybackListeners.forEach { listener ->
                                listener.onChangePosition(positionInfo)
                            }
                        }

                        if (positionInfo.isFinished() && player.state == PlaybackState.Stopped) {
                            onWaitingNextTrack()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopTimer() {
        job.cancel()
    }

    private suspend fun onWaitingNextTrack() {
        when (repeatMode) {
            PlayerRepeatMode.NONE -> handleCommand(PlayerCommand.Next)
            PlayerRepeatMode.ONE_TRACK -> tryPlayCurrentItem()
            PlayerRepeatMode.PLAYLIST -> {
                if (currentItem == playlist?.last()) {
                    tryChangeIndexAndPlayAudio(0)
                } else {
                    handleCommand(PlayerCommand.Next)
                }
            }
        }
    }

    private fun changeRepeatMode(mode: PlayerRepeatMode) {
        log.d(this, "change repeat mode to $mode")
        repeatMode = mode
        saveConfig()
        externalPlaybackListeners.forEach { listener ->
            listener.onChangeRepeatMode(repeatMode)
        }
    }

    private fun saveConfig() {
        ioScope.launch {
            configRepository.apply {
                get().repeatMode = repeatMode
                get().isShuffleMode = isShuffleMode
                save()
            }
        }
    }

    private fun getRealPlaylistIndex(index: Int): Int {
        return if (isShuffleMode) {
            if (mapToRealIndex == null || playlist?.size != mapToRealIndex?.size) {
                generateShuffleData()
            }
            mapToRealIndex?.get(index) ?: -1
        } else {
            index
        }
    }

    private fun shuffleNewIndexIfNeed(newIndex: Int): Int {
        return if (isShuffleMode) {
            randomQueue.orEmpty().getOrNull(newIndex) ?: -1
        } else {
            newIndex
        }
    }

    private fun generateShuffleData() {
        randomQueue = buildList {
            playlist?.indexOf(currentItem)?.let { add(it) }
            addAll(
                playlist.orEmpty().mapIndexed { index, item ->
                    if (item == currentItem) null else index
                }.filterNotNull().shuffled()
            )
        }
        mapToRealIndex = buildMap {
            randomQueue?.forEachIndexed { index, item ->
                put(item, index)
            }
        }
    }

    private fun releaseShuffleData() {
        randomQueue = null
        mapToRealIndex = null
    }

}
