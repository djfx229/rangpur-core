package io.github.djfx229.rangpur.core.feature.player.domain.model

import io.github.djfx229.rangpur.core.common.domain.util.DurationFormat

class PlayerPosition(
    /**
     * Позиция воспроизведения в секундах.
     */
    val position: Double,

    /**
     * Продолжительность текущего трека в секундах.
     */
    val duration: Double,
) {

    fun isFinished(): Boolean {
        return position == duration
    }

    fun formatedPosition(): String {
        return DurationFormat.format(position.toLong())
    }

    fun formatedDuration(): String {
        return DurationFormat.format(duration.toLong())
    }

    override fun toString(): String {
        return "position: ${formatedPosition()}, duration: ${formatedDuration()}"
    }

}
