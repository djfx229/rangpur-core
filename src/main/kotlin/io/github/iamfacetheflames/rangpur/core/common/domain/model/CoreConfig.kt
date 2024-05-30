package io.github.iamfacetheflames.rangpur.core.common.domain.model

/**
 * Модель конфига с общими полями для всех приложений базирующихся на core.
 */
data class CoreConfig(
    var musicLibraryPath: String?
) : Config