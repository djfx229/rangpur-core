package io.github.djfx229.rangpur.feature.sync.domain.model

import io.github.djfx229.rangpur.feature.library.domain.model.Audio

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