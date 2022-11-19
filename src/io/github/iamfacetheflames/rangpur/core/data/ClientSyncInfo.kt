package io.github.iamfacetheflames.rangpur.core.data

sealed class ClientSyncInfo
data class ClientConnectedSyncInfo(
    val hostAddress: String,
) : ClientSyncInfo()
data class ReceivingAudio(
    val audio: Audio,
    val progress: Int,
    val amount: Int
): ClientSyncInfo()