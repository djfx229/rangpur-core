package io.github.iamfacetheflames.rangpur.core.common.domain.interactor

import io.github.iamfacetheflames.rangpur.core.common.domain.di.DependencyInjector
import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.data.Directory

class AudioInteractor(
    private val di: DependencyInjector,
) {

    private val directoryInteractor by lazy {
        di.get<DirectoryInteractor>()
    }

    fun getFullPath(audio: Audio, cacheDirectories: HashMap<String, Directory>? = null): String {
        val path = directoryInteractor.getFullPath(
            audio.directoryUUID,
            cacheDirectories,
        )
        // не нужно добавлять разделитель - locationInMusicDirectory всегда заканчивается со слэшем
        return path + audio.fileName
    }

}