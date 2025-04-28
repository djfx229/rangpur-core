package io.github.iamfacetheflames.rangpur.core.feature.player.domain.model.state

import io.github.iamfacetheflames.rangpur.core.feature.radio.domain.model.RadioStation
import io.github.iamfacetheflames.rangpur.core.feature.radio.domain.model.StreamMetadata
import io.github.iamfacetheflames.rangpur.core.data.Audio

sealed class MetadataState

object EmptyMetadataState : MetadataState()

/**
 * Метаданные для онлайн-радио.
 *
 * Если metadata == null, значит плеер ещё не получил информацию о метаданных из потока,
 * например идёт подключение к радиостанции.
 */
data class StreamMetadataState(
    val radioStation: RadioStation,
    val metadata: StreamMetadata? = null,
) : MetadataState()

data class AudioMetadataState(
    val audio: Audio,
) : MetadataState()