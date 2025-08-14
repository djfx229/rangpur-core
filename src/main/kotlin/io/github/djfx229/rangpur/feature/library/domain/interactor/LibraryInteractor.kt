package io.github.djfx229.rangpur.feature.library.domain.interactor

import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.common.domain.di.getConfigRepository
import io.github.djfx229.rangpur.common.domain.model.CoreConfig
import io.github.djfx229.rangpur.common.domain.model.sort.Sort
import io.github.djfx229.rangpur.common.domain.repository.ConfigRepository
import io.github.djfx229.rangpur.common.domain.database.Database
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.feature.library.domain.model.Directory
import io.github.djfx229.rangpur.feature.library.domain.model.copy
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.Filter
import io.github.djfx229.rangpur.feature.library.domain.model.filter.LegacyFilter
import io.github.djfx229.rangpur.feature.library.domain.repository.LibraryRepository
import java.io.File

class LibraryInteractor(
    private val di: DependencyInjector,
) {

    private val directoryInteractor: DirectoryInteractor by lazy { di.get()}
    private val repository: LibraryRepository by lazy { di.get() }
    private val database: Database by lazy { di.get() }
    private val configRepository: ConfigRepository<CoreConfig> by lazy { di.getConfigRepository() }

    fun getMusicDirectoryLocation(): String {
        return configRepository.get().musicLibraryPath
    }

    fun getAudios(filter: Filter, sort: Sort): List<Audio> {
        return repository.getAudios(filter, sort)
    }

    fun getFullPath(
        audio: Audio,
        cacheDirectories: HashMap<String, Directory>? = null,
    ): String {
        val path = directoryInteractor.getFullPath(
            audio.directoryUUID,
            cacheDirectories,
        )
        // не нужно добавлять разделитель - locationInMusicDirectory всегда заканчивается со слэшем
        return path + audio.fileName
    }

    fun getDirectoryForAudio(audio: Audio): Directory {
        return database.directories.getItem(audio.directoryUUID)
    }

    /***
     * Чтобы не вносить изменения в оригинальную аудиозапись - делается ее копия.
     */
    fun copyAudio(audio: Audio): Audio {
        return audio.copy(database.getBuilder())
    }

    fun updateAudio(audio: Audio) {
        return database.audios.update(listOf(audio))
    }

    fun getAudiosByLegacyFilter(filter: LegacyFilter): List<Audio> {
        return database.audios.getFiltered(filter)
    }

    fun getAudioFile(audio: Audio): File {
        return File(getFullPath(audio))
    }

    fun getFilesForAudios(
        audios: List<Audio>
    ): List<File> {
        val cacheDirectories: HashMap<String, Directory> = hashMapOf()
        val files: MutableList<File> = mutableListOf()
        for (audio in audios) {
            try {
                files.add(File(getFullPath(audio)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cacheDirectories.clear()
        return files
    }

}
