package io.github.iamfacetheflames.rangpur.core.router


interface PlaylistRouter : Router {
    suspend fun openInputDialog(message: String, defaultValue: String? = null): String?
}