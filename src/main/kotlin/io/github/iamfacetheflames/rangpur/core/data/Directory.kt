package io.github.iamfacetheflames.rangpur.core.data

import io.github.iamfacetheflames.rangpur.core.repository.Configuration
import java.io.File

interface Directory : WithId {
    var parent: Directory?
    var locationInMusicDirectory: String?
    var name: String?
}

const val universalSeparator = "/"

fun Directory.getJavaFile(config: Configuration): File {
    val libraryLocation = config.getMusicDirectoryLocation()
    val fullPath = libraryLocation + this.locationInMusicDirectory
    val file = File(fullPath)
    return file
}