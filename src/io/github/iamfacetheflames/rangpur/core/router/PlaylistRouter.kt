package io.github.iamfacetheflames.rangpur.core.router

import io.github.iamfacetheflames.rangpur.core.presenter.Router

interface PlaylistRouter : Router {
    suspend fun openInputDialog(message: String, defaultValue: String? = null): String?
}