package io.github.iamfacetheflames.rangpur.core.router

import java.io.File

interface LibraryRouter : Router {
    fun openM3uOnExternalApp(file: File)
    fun openFileManager(file: File)
}