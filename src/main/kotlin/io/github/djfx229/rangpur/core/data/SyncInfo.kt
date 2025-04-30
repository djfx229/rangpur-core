package io.github.djfx229.rangpur.core.data

import io.github.djfx229.rangpur.feature.audio.domain.model.Audio

sealed class SyncInfo
object SyncInfoFinished : SyncInfo()
data class SyncInfoClientConnected(
    val hostAddress: String,
) : SyncInfo()
data class SyncInfoReceivingAudio(
    val audio: Audio,
    val progress: Int,
    val amount: Int
): SyncInfo()