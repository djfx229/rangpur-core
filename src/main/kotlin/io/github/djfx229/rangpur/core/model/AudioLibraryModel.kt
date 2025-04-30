package io.github.djfx229.rangpur.core.model

import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.core.data.*
import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.feature.library.domain.model.Directory
import io.github.djfx229.rangpur.feature.library.domain.interactor.LibraryInteractor
import java.io.File


class AudioLibraryModel(
    private val di: DependencyInjector,
) {

    private val database: Database by lazy { di.get() }
    private val libraryInteractor: LibraryInteractor by lazy { di.get() }

    fun getAllAudios() = database.audios.getAll()

    fun getAudios(filter: Filter): List<Audio> {
        return database.audios.getFiltered(filter)
    }

    fun getAudioFile(audio: Audio): File {
        return File(libraryInteractor.getFullPath(audio))
    }

    fun getFilesForAudios(
        audios: List<Audio>
    ): List<File> {
        val cacheDirectories: HashMap<String, Directory> = hashMapOf()
        val files: MutableList<File> = mutableListOf()
        for (audio in audios) {
            try {
                files.add(File(libraryInteractor.getFullPath(audio)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cacheDirectories.clear()
        return files
    }

}
