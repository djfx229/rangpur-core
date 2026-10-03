package io.github.djfx229.rangpur.feature.player.domain.interactor

import io.github.djfx229.rangpur.common.domain.Logger
import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.common.domain.di.getConfigRepository
import io.github.djfx229.rangpur.common.domain.repository.ConfigRepository
import io.github.djfx229.rangpur.feature.library.domain.interactor.LibraryInteractor
import io.github.djfx229.rangpur.feature.player.domain.controller.PlayerController
import io.github.djfx229.rangpur.feature.player.domain.model.*
import io.github.djfx229.rangpur.feature.player.domain.model.state.MetadataState
import io.github.djfx229.rangpur.feature.player.domain.model.state.PlaybackState
import io.github.djfx229.rangpur.feature.radio.domain.model.RadioStation
import io.github.djfx229.rangpur.feature.radio.domain.model.StreamMetadata
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.Double.min
import kotlin.math.max

class PlayerInteractor(
    private val di: DependencyInjector,
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val playbackDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {

    interface Listener {
        fun onChangePosition(info: PlayerPosition) {}
        fun onChangeState(state: PlaybackState) {}
        fun onChangeMetadata(state: MetadataState) {}
        fun onChangeRepeatMode(mode: PlayerRepeatMode) {}
        fun onChangeShuffleMode(isShuffleMode: Boolean) {}
    }

    private val log: Logger by lazy { di.get() }

    private val player by lazy {
        di.get(PlayerController::class)
    }

    private val libraryInteractor: LibraryInteractor by lazy { di.get() }

    private val configRepository: ConfigRepository<PlayerConfig> by lazy {
        di.getConfigRepository()
    }

    private var playerQueue: PlayerQueue<*> = PlayerQueue.Empty
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
            log.d(this, "onPlay()")
            playerQueue.currentItem?.let {
                setPlaybackState(PlaybackState.Playing)
            }
        }

        override fun onPause() {
            log.d(this, "onPause()")
            playerQueue.currentItem?.let {
                setPlaybackState(PlaybackState.Paused)
            }
        }

        override fun onChangeStreamMetadata(metadata: StreamMetadata) {
            log.d(this, "onChangeStreamMetadata() // metadata=$metadata")
            playerQueue.currentItem?.let { item ->
                if (item is RadioStation) {
                    setMetadataState(
                        MetadataState.Stream(item, metadata)
                    )
                }
            }
        }
    }

    val state: PlaybackState
        get() = player.state

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

    private fun tryPlayCurrentItem() {
        if (playerQueue.currentItem == null) return
        setMetadataState(playerQueue.currentMetadata)
        player.open(playerQueue.currentPlayerSource(libraryInteractor))
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
                is PlayerCommand.Open -> handleCommandOpen(command)
                PlayerCommand.Play -> handleCommandPlay()
                PlayerCommand.TogglePlayOrPause -> handleCommandPause()
                PlayerCommand.Stop -> handleCommandStop()

                is PlayerCommand.Next -> handleCommandNext(command)
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

    private fun handleCommandOpen(command: PlayerCommand.Open) {
        val lastItem = playerQueue.currentItem
        if (playerQueue != command.queue) {
            playerQueue = command.queue
        }
        player.setListener(playerListener)
        if (command.doNotRestartPlaybackForSameTrack && lastItem == playerQueue.currentItem) return
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

    private fun handleCommandNext(command: PlayerCommand.Next) {
        playerQueue.switchTo(
            where = SwitchDirection.Next,
            isShuffleModeOn = isShuffleMode,
            isInfinityModeOn = repeatMode == PlayerRepeatMode.PLAYLIST,
            isFullListened = command.reason == SwitchReason.TrackFinished,
            onSuccessfullySwitched = {
                tryPlayCurrentItem()
            }
        )
    }

    private fun handleCommandPrevious() {
        playerQueue.switchTo(
            where = SwitchDirection.Previous,
            isShuffleModeOn = isShuffleMode,
            isInfinityModeOn = repeatMode == PlayerRepeatMode.PLAYLIST,
            isFullListened = false,
            onSuccessfullySwitched = {
                tryPlayCurrentItem()
            }
        )
    }

    private fun handleCommandSeekTo(command: PlayerCommand.SeekTo) {
        if (playerQueue.isSupportsSeek) {
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
        val bpm = playerQueue.currentAudio?.bpm ?: return
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

        saveConfig()
        externalPlaybackListeners.forEach { listener ->
            listener.onChangeShuffleMode(isShuffleMode)
        }
    }

    private fun startTimer() {
        stopTimer()
        job = SupervisorJob()
        playbackScope = CoroutineScope(playbackDispatcher + job)
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
            PlayerRepeatMode.ONE_TRACK -> tryPlayCurrentItem()
            else -> handleCommand(PlayerCommand.Next(SwitchReason.TrackFinished))
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

}
