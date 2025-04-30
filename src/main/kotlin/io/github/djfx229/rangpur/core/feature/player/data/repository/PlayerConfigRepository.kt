package io.github.djfx229.rangpur.core.feature.player.data.repository

import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.PlayerConfig
import io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.PlayerRepeatMode
import io.github.iamfacetheflames.rangpur.core.common.data.repository.JsonConfigRepository
import io.github.iamfacetheflames.rangpur.core.common.domain.Logger
import io.github.iamfacetheflames.rangpur.core.common.domain.model.ApplicationConfig
import java.lang.reflect.Type

class PlayerConfigRepository(
    logger: Logger,
    applicationConfig: ApplicationConfig,
) : JsonConfigRepository<PlayerConfig>(logger, applicationConfig) {

    override fun jsonFilePath(): String = "/PlayerConfig.json"

    override fun defaultConfig(): PlayerConfig {
        return PlayerConfig().apply {
            repeatMode = PlayerRepeatMode.NONE
        }
    }

    override fun configType(): Type = PlayerConfig::class.java

}

