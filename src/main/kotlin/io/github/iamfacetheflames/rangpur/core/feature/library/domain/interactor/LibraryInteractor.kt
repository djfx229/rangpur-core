package io.github.iamfacetheflames.rangpur.core.feature.library.domain.interactor

import io.github.iamfacetheflames.rangpur.core.common.domain.di.DependencyInjector
import io.github.iamfacetheflames.rangpur.core.common.domain.interactor.DirectoryInteractor
import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.data.Directory
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.model.filter.Filter
import io.github.iamfacetheflames.rangpur.core.common.domain.model.sort.Sort
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.repository.LibraryRepository

class LibraryInteractor(
    private val di: DependencyInjector,
) {

    private val directoryInteractor by lazy {
        di.get<DirectoryInteractor>()
    }

    private val repository by lazy {
        di.get<LibraryRepository>()
    }

    fun getFullPath(audio: Audio, cacheDirectories: HashMap<String, Directory>? = null): String {
        val path = directoryInteractor.getFullPath(
            audio.directoryUUID,
            cacheDirectories,
        )
        // не нужно добавлять разделитель - locationInMusicDirectory всегда заканчивается со слэшем
        return path + audio.fileName
    }

    fun getAudios(filter: Filter, sort: Sort): List<Audio> {
        return repository.getAudios(filter, sort)
    }

}