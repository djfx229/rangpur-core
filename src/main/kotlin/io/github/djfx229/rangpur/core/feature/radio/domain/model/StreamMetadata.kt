package io.github.djfx229.rangpur.core.feature.radio.domain.model

/**
 * Информация о интернет-потоке, который воспроизводит в данный момент времени аудиоплеер.
 */
data class StreamMetadata(
    val station: String?,
    val genre: String?,
    val title: String?,
)
