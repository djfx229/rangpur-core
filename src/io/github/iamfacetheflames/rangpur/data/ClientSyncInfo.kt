package io.github.iamfacetheflames.rangpur.data

sealed class ClientSyncInfo
data class ReceivingAudio(
    val audio: Audio,
    val progress: Int,
    val amount: Int
): ClientSyncInfo()