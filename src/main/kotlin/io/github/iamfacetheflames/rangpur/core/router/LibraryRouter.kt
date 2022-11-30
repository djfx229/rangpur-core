package io.github.iamfacetheflames.rangpur.core.router

import java.io.File

@Deprecated(message = "Будет удалено после отказа от router")
interface LibraryRouter {
    fun openM3uOnExternalApp(file: File)
    fun openFileManager(file: File)
    fun openSaveFileDialog(
        message: String,
        defaultName: String,
        fileDescription: String,
        vararg fileExtensions: String
    ): File?
}