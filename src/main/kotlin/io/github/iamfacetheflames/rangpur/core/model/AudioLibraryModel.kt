package io.github.iamfacetheflames.rangpur.core.model

import io.github.iamfacetheflames.rangpur.core.data.*
import io.github.iamfacetheflames.rangpur.core.repository.database.Database
import io.github.iamfacetheflames.rangpur.core.repository.Configuration
import java.io.File

class AudioLibraryModel(
    private val database: Database,
    private val config: Configuration
) {

    fun getAllAudios() = database.audios.getAll()

    fun getAudios(filter: Filter): List<Audio> {
        return database.audios.getFiltered(filter)
    }

    fun getAudios(playlist: Playlist): List<AudioInPlaylist> {
        return database.playlistWithAudios.getFrom(playlist)
    }

    suspend fun createM3u8PlaylistWithFilteredAudios(
        directoryForM3u: File,
        fileName: String,
        filter: Filter
    ): File {
        val audios = getAudios(filter)
        val cachedDirectories = CachedDirectories(database.directories, config)
        val file = PlaylistToFile.exportPlaylistM3u8(
            fileName,
            directoryForM3u.absolutePath,
            audios,
            cachedDirectories
        )
        cachedDirectories.release()
        return file
    }

    suspend fun getFullPath(audio: Audio): File? {
        val directory = database.directories.getItem(audio.directoryUUID)
        return if (directory != null) {
            File(directory.getJavaFile(config).path + universalSeparator + audio.fileName)
        } else {
            null
        }
    }

    fun getSelectedAudios(
        selectedRows: IntArray,
        audios: List<AudioInPlaylist>
    ): Pair<
            MutableList<AudioInPlaylist>,
            MutableList<File>
            > {
        val selectedList = mutableListOf<AudioInPlaylist>()
        val cachedDirs = CachedDirectories(database.directories, config)
        val files: MutableList<File> = mutableListOf<File>()
        for(selIndex in selectedRows) {
            audios.getOrNull(selIndex)?.let {
                it.audio?.let { audio ->
                    try {
                        files.add(File(cachedDirs.getFullAudioPath(audio)))
                        selectedList.add(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        cachedDirs.release()
        return Pair(selectedList, files)
    }

}