package io.github.iamfacetheflames.rangpur.core.presenter

import java.io.File

interface Router {

    suspend fun openSaveFileDialog(
        message: String,
        defaultName: String,
        fileDescription: String,
        vararg fileExtensions: String
    ): File?

    suspend fun showErrorMessage(
        message: String,
        title: String = "Ошибка"
    )

}

