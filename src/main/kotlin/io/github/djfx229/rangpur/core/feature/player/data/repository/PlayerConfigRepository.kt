package io.github.djfx229.rangpur.core.feature.player.data.repository

import io.github.djfx229.rangpur.core.feature.player.domain.model.PlayerConfig
import io.github.djfx229.rangpur.core.feature.player.domain.model.PlayerRepeatMode
import io.github.djfx229.rangpur.core.common.data.repository.JsonConfigRepository
import io.github.djfx229.rangpur.core.common.domain.Logger
import io.github.djfx229.rangpur.core.common.domain.model.ApplicationConfig
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

