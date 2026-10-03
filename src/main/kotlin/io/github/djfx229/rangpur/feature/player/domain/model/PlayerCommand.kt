package io.github.djfx229.rangpur.feature.player.domain.model

import io.github.djfx229.rangpur.feature.player.domain.interactor.PlayerQueue

enum class SwitchReason {
    UserAction,
    TrackFinished,
}

sealed class PlayerCommand {

    data class Open(
        val queue: PlayerQueue<*>,
        val doNotRestartPlaybackForSameTrack: Boolean = false,
    ) : PlayerCommand()

    data object Play : PlayerCommand()

    data object TogglePlayOrPause : PlayerCommand()

    data object Stop : PlayerCommand()

    data object Previous : PlayerCommand()

    data class Next(
        val reason: SwitchReason = SwitchReason.UserAction,
    ) : PlayerCommand()

    data class SeekTo(
        val positionSeconds: Double,
    ) : PlayerCommand()

    /**
     * Смещает текущую позицию воспроизведение на [relativePositionSeconds]
     *
     * relativePositionSeconds = 5.0 сместит на 5ть секунд вперёд
     * relativePositionSeconds = -5.0 сместит на 5ть секунд назад
     */
    data class RelativeSeek(
        val relativePositionSeconds: Double,
    ) : PlayerCommand()

    /**
     * Смещение по тактам
     *
     * beats = 16 сместит на один квадрат вперёд
     * beats = -16 сместит на один квадрат назад
     * если bpm трека не определён, то игнорируется
     */
    data class BeatsSeek(
        val beats: Int,
    ) : PlayerCommand()

    /**
     * Выключает плеер, высвобождает занятые им ресурсы.
     */
    data object Release : PlayerCommand()

    data object ToggleRepeatMode : PlayerCommand()

    data object ToggleShuffleMode : PlayerCommand()

}
