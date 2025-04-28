package io.github.iamfacetheflames.rangpur.core.feature.player.domain.interactor

import io.github.iamfacetheflames.rangpur.core.feature.player.domain.controller.PlayerController
import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.state.MetadataState
import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.state.PlaybackState
import io.github.iamfacetheflames.rangpur.core.feature.radio.domain.model.StreamMetadata
import io.github.iamfacetheflames.rangpur.core.common.domain.di.DependencyInjector
import io.github.iamfacetheflames.rangpur.core.common.domain.interactor.AudioInteractor
import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.data.AudioInPlaylist
import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.PlayerSource
import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.*
import io.github.iamfacetheflames.rangpur.core.feature.radio.domain.model.RadioStation
import kotlinx.coroutines.*
import java.io.File
import java.lang.Double.min


class PlayerInteractor(
    private val di: DependencyInjector,
) {

    interface Listener {
        fun onChangePosition(info: PlayerPosition)
        fun onChangeState(state: PlaybackState) {}
        fun onChangeMetadata(state: MetadataState) {}
        fun onChangeCurrentIndex(index: Int, item: Any) {}
    }

    private val player by lazy {
        di.get(PlayerController::class)
    }

    private val audioInteractor by lazy {
        di.get(AudioInteractor::class)
    }

    private var playlist: List<Any>? = null
    private var currentIndex: Int = -1
    private var currentPlaybackState: PlaybackState = PlaybackState.Stopped
    private var currentMetadataState: MetadataState = MetadataState.EmptyMetadataState
    private var externalPlaybackListeners = emptyList<Listener>().toMutableList()

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

    var repeatMode: PlayerRepeatMode = PlayerRepeatMode.NONE

    var currentItem: Any? = null
        private set

    fun addListener(listener: Listener) {
        externalPlaybackListeners.add(listener)
        listener.onChangeState(currentPlaybackState)
        listener.onChangeMetadata(currentMetadataState)
    }

    fun removeListener(listener: Listener) {
        externalPlaybackListeners.remove(listener)
    }

    fun getPlayerPosition(): PlayerPosition = player.getPosition()

    private fun tryChangeIndexAndPlayAudio(newIndex: Int) {
        val size = playlist?.size ?: return
        if (newIndex in 0 until size) {
            currentItem = playlist?.getOrNull(newIndex) ?: return
            currentIndex = newIndex
            tryPlayCurrentItem()
        }
    }

    private fun tryPlayCurrentItem() {
        val item = currentItem ?: return

        externalPlaybackListeners.forEach { listener ->
            listener.onChangeCurrentIndex(currentIndex, item)
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

    fun handleCommand(command: PlayerCommand) {
        when (command) {
            is PlayerCommand.Open<*> -> {
                playlist = command.items.filterNotNull()
                currentIndex = command.index
                this.currentItem = command.currentItem

                player.setListener(playerListener)
                tryPlayCurrentItem()
            }

            PlayerCommand.Play -> {
                if (player.state == PlaybackState.Paused) {
                    player.play()
                } else {
                    tryPlayCurrentItem()
                }
            }

            PlayerCommand.Pause -> {
                if (player.state == PlaybackState.Paused) {
                    player.play()
                } else {
                    player.pause()
                }
            }

            PlayerCommand.Stop -> {
                player.stop()
                stopTimer()
                setPlaybackState(PlaybackState.Stopped)
            }

            PlayerCommand.Next -> {
                tryChangeIndexAndPlayAudio(currentIndex + 1)
            }

            PlayerCommand.Previous -> {
                tryChangeIndexAndPlayAudio(currentIndex - 1)
            }

            is PlayerCommand.SeekTo -> {
                if (currentItem is Audio || currentItem is AudioInPlaylist || currentItem is File) {
                    player.seekTo(command.positionSeconds)
                }
            }

            is PlayerCommand.RelativeSeek -> {
                val positionInfo = player.getPosition()
                val newPosition = min(
                    positionInfo.position + command.relativePositionSeconds,
                    if (command.relativePositionSeconds > 0) positionInfo.duration else 0.0
                )
                handleCommand(PlayerCommand.SeekTo(newPosition))
            }

            is PlayerCommand.BeatsSeek -> {
                val item = currentItem
                val bpm = when (item) {
                    is Audio -> item.bpm
                    is AudioInPlaylist -> item.audio?.bpm
                    else -> null
                } ?: return
                val beatsToSeconds = (bpm / 60.0) * command.beats
                handleCommand(PlayerCommand.RelativeSeek(beatsToSeconds))
            }

            PlayerCommand.Release -> {
                stopTimer()
                player.release()
            }
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

    private fun onWaitingNextTrack() {
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

}
