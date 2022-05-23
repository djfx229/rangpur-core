package io.github.iamfacetheflames.rangpur.core.router

import io.github.iamfacetheflames.rangpur.core.presenter.Router
import java.io.File

interface LibraryRouter : Router {
    fun openM3uOnExternalApp(file: File)
    fun openFileManager(file: File)
}