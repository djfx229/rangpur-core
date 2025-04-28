package io.github.iamfacetheflames.rangpur.core.feature.player.domain.model

sealed class PlayerSource  {
    object Unsupported : PlayerSource()

    data class File(
        val filePath: String,
    ) : PlayerSource()

    class Stream(
        val streamUrl: String,
    ) : PlayerSource()
}
