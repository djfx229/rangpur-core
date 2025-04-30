package io.github.djfx229.rangpur.feature.player.domain.model

import io.github.djfx229.rangpur.common.domain.model.Config

class PlayerConfig : Config {
    var repeatModeOrdinal: Int = -1

    var isShuffleMode: Boolean = false

    var repeatMode: PlayerRepeatMode
        get() {
            return PlayerRepeatMode.values().getOrElse(repeatModeOrdinal) { PlayerRepeatMode.NONE }
        }
        set(value) {
            repeatModeOrdinal = value.ordinal
        }
}