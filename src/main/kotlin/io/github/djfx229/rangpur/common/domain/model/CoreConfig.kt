package io.github.djfx229.rangpur.common.domain.model

/**
 * Модель конфига с общими полями для всех приложений базирующихся на core.
 */
data class CoreConfig(
    var musicLibraryPath: String?
) : Config