package io.github.iamfacetheflames.rangpur.presenter

import java.io.File

interface Router {
    suspend fun showErrorMessage(
        message: String,
        title: String = "Ошибка"
    )
    suspend fun openInputDialog(message: String, defaultValue: String? = null): String?
    suspend fun openSaveFileDialog(
        message: String,
        defaultName: String,
        fileDescription: String,
        vararg fileExtensions: String
    ): File?
    fun openFileManager(file: File)
    fun openM3uOnExternalApp(file: File)
}