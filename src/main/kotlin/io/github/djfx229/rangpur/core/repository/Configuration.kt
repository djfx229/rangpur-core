package io.github.djfx229.rangpur.core.repository

import io.github.djfx229.rangpur.common.domain.model.Config

@Deprecated(message = "Используйте для получения пути до audio library репозиторий - ConfigRepository<CoreConfig>")
interface Configuration : Config {

    fun getMusicDirectoryLocation(): String

}