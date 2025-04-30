package io.github.djfx229.rangpur.core.feature.player.domain.controller

import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.PlayerPosition
import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.PlayerSource
import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.state.PlaybackState
import io.github.iamfacetheflames.rangpur.core.feature.radio.domain.model.StreamMetadata

interface PlayerController {

    interface Listener {
        fun onPlay()
        fun onPause()
        fun onStop() {}
        fun onChangeStreamMetadata(metadata: StreamMetadata)
        fun onError(message: String) {}
    }

    val state: PlaybackState

    fun open(source: PlayerSource)

    fun play()

    fun pause()

    fun stop()

    fun setListener(listener: Listener)

    fun getPosition(): PlayerPosition

    /**
     * Останавливает плеер, высвобождает занятые им ресурсы.
     */
    fun release()

    /**
     * Перемещает текущую позицию плеера.
     *
     * Позиция задаётся в секундах.
     */
    fun seekTo(seconds: Double)

    /**
     * Плеер проинициализирован и готов к воспроизведению аудио.
     */
    fun isPlayerReady(): Boolean

}
