package io.github.djfx229.rangpur.core.router

import java.io.File

@Deprecated(message = "Будет удалено после отказа от router")
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