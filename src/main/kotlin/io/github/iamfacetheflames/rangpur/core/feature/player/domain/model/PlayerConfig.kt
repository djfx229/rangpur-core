package io.github.iamfacetheflames.rangpur.core.feature.player.domain.model

import io.github.iamfacetheflames.rangpur.core.common.domain.model.Config

class PlayerConfig : Config {
    var repeatModeOrdinal: Int = -1

    var repeatMode: PlayerRepeatMode
        get() {
            return PlayerRepeatMode.values().getOrElse(repeatModeOrdinal) { PlayerRepeatMode.NONE }
        }
        set(value) {
            repeatModeOrdinal = value.ordinal
        }
}