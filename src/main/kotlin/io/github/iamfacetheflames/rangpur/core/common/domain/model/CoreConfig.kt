package io.github.iamfacetheflames.rangpur.core.common.domain.model

/**
 * Модель конфига общая для всех приложений базирующихся на core.
 */
data class CoreConfig(
    var musicLibraryPath: String? = null,
) : Config