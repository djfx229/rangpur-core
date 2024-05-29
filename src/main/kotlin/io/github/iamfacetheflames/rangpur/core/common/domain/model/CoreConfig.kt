package io.github.iamfacetheflames.rangpur.core.common.domain.model

/**
 * Интерфейс конфига с общими полями для всех приложений базирующихся на core.
 */
interface CoreConfig : Config {
    var musicLibraryPath: String?
}