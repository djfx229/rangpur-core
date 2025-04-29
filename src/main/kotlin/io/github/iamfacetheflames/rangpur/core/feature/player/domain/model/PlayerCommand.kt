package io.github.iamfacetheflames.rangpur.core.feature.player.domain.model

sealed class PlayerCommand {

    data class Open<T : Any>(
        val index: Int,
        val currentItem: T,
        val items: List<T>,
    ) : PlayerCommand()

    object Play : PlayerCommand()

    object Pause : PlayerCommand()

    object Stop : PlayerCommand()

    object Previous : PlayerCommand()

    object Next : PlayerCommand()

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
    object Release : PlayerCommand()

    object ToggleRepeatMode : PlayerCommand()

    object ToggleShuffleMode : PlayerCommand()

}
