package io.github.djfx229.rangpur.common.domain.interactor

import io.github.djfx229.rangpur.core.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.core.data.Audio
import io.github.djfx229.rangpur.core.data.Directory
import io.github.djfx229.rangpur.core.data.copy
import io.github.djfx229.rangpur.core.repository.database.Database

class AudioInteractor(
    private val di: DependencyInjector,
) {

    private val directoryInteractor by lazy {
        di.get<DirectoryInteractor>()
    }

    private val database by lazy {
        di.get<Database>()
    }

    fun getFullPath(audio: Audio, cacheDirectories: HashMap<String, Directory>? = null): String {
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

}
