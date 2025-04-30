package io.github.djfx229.rangpur.core.data

import io.github.djfx229.rangpur.core.repository.Configuration
import java.io.File
import java.sql.Date

interface Directory : WithId {
    var parent: Directory?

    /**
     * Относительный путь до директории (внутри директории фонотеки)
     *
     * Должен начинаться со слэша.
      */
    var locationInMusicDirectory: String?

    var name: String?
}

fun Directory.equalsAllFields(other: Any?): Boolean {
    return if (other is Directory) {
        other.uuid == this.uuid &&
        other.parent == this.parent &&
        other.locationInMusicDirectory == this.locationInMusicDirectory &&
        other.name == this.name
    } else {
        false
    }
}

class TestDirectory(
    override var name: String?,
    override var locationInMusicDirectory: String?,
    override var parent: Directory? = null,
    override var uuid: String = generateUuid(),
) : TestItem(), Directory

const val universalSeparator = "/"

fun Directory.getJavaFile(config: Configuration): File {
    val libraryLocation = config.getMusicDirectoryLocation()
    val fullPath = libraryLocation + this.locationInMusicDirectory
    val file = File(fullPath)
    return file
}