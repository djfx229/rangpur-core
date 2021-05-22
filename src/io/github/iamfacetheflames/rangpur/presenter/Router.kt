package io.github.iamfacetheflames.rangpur.presenter

import java.io.File

interface Router {
    suspend fun openInputDialog(message: String, defaultValue: String? = null): String?
    suspend fun openDirectoryChooserDialog(message: String): File?
    suspend fun openSaveFileDialog(
        message: String,
        defaultName: String,
        fileDescription: String,
        vararg fileExtensions: String
    ): File?
    fun openM3uOnExternalApp(file: File)
    fun openAudioOnPreviewPlayer(file: File)
    fun openFileManager(file: File)
}