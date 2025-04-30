package io.github.djfx229.rangpur.core.model

import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.common.domain.interactor.DirectoryInteractor
import io.github.djfx229.rangpur.feature.audio.domain.model.Audio
import io.github.djfx229.rangpur.feature.audio.domain.model.Directory
import io.github.djfx229.rangpur.feature.library.domain.interactor.LibraryInteractor

class CachedDirectories(
    private val di: DependencyInjector,
) {

    private val directoryInteractor: DirectoryInteractor by lazy { di.get() }
    private val libraryInteractor: LibraryInteractor by lazy { di.get() }

    private val cache: HashMap<String, Directory> =
        HashMap()

    fun getFullDirectoryPath(directoryUUID: String): String {
        return directoryInteractor.getFullPath(directoryUUID, cache)
    }

    fun getFullAudioPath(audio: Audio): String {
        return libraryInteractor.getFullPath(audio, cache)
    }

    fun release() {
        cache.clear()
    }

}
