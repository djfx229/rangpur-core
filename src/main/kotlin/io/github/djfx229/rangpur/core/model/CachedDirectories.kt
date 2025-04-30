package io.github.djfx229.rangpur.core.model

import io.github.djfx229.rangpur.core.data.Audio
import io.github.djfx229.rangpur.core.data.Directory
import io.github.djfx229.rangpur.core.repository.Configuration
import io.github.djfx229.rangpur.core.repository.database.Database
import kotlin.collections.HashMap

class CachedDirectories(
    private val databaseDirectories: Database.Directories,
    private val config: Configuration
) {

    private val cache: HashMap<String, Directory> =
        HashMap()

    fun getFullDirectoryPath(directoryUUID: String): String {
        val directory = getDirectory(directoryUUID)
        // не нужно добавлять разделитель - locationInMusicDirectory всегда начинается со слэша
        return config.getMusicDirectoryLocation() + directory.locationInMusicDirectory
    }

    fun getFullAudioPath(audio: Audio): String {
        val path = getFullDirectoryPath(
            audio.directoryUUID
        )
        // не нужно добавлять разделитель - locationInMusicDirectory всегда заканчивается со слэшем
        return path + audio.fileName
    }

    fun release() {
        cache.clear()
    }

    private fun getDirectory(directoryUUID: String): Directory {
        val cachedDirectory = cache[directoryUUID]
        return if (cachedDirectory == null) {
            val directoryFromDB = databaseDirectories.getItem(directoryUUID)
            cache[directoryUUID] = directoryFromDB
            directoryFromDB
        } else {
            cachedDirectory
        }
    }

}