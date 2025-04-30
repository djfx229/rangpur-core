package io.github.djfx229.rangpur.core.model

import io.github.djfx229.rangpur.core.data.*
import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.core.repository.Configuration
import io.github.djfx229.rangpur.feature.audio.domain.model.Audio
import io.github.djfx229.rangpur.feature.audio.domain.model.getJavaFile
import io.github.djfx229.rangpur.feature.audio.domain.model.universalSeparator
import io.github.djfx229.rangpur.feature.playlist.domain.model.AudioInPlaylist
import io.github.djfx229.rangpur.feature.playlist.domain.model.Playlist
import java.io.File

@Deprecated("Переходим на LibraryInteractor")
class AudioLibraryModel(
    private val database: Database,
    private val config: Configuration
) {

    fun getAllAudios() = database.audios.getAll()

    fun getAudios(filter: Filter): List<Audio> {
        return database.audios.getFiltered(filter)
    }



    suspend fun getFullPath(audio: Audio): File? {
        val directory = database.directories.getItem(audio.directoryUUID)
        return if (directory != null) {
            File(directory.getJavaFile(config).path + universalSeparator + audio.fileName)
        } else {
            null
        }
    }

    fun getFilesForAudios(
        audios: List<Audio>
    ): List<File> {
        val cachedDirs = CachedDirectories(database.directories, config)
        val files: MutableList<File> = mutableListOf()
        for (audio in audios) {
            try {
                files.add(File(cachedDirs.getFullAudioPath(audio)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cachedDirs.release()
        return files
    }

}