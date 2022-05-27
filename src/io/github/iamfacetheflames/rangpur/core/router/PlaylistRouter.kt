package io.github.iamfacetheflames.rangpur.core.router

import java.io.File


interface PlaylistRouter {
    fun showErrorMessage(
        message: String,
        title: String = "Ошибка"
    )
    fun openSaveFileDialog(
        message: String,
        defaultName: String,
        fileDescription: String,
        vararg fileExtensions: String
    ): File?
    suspend fun openInputDialog(message: String, defaultValue: String? = null): String?
}