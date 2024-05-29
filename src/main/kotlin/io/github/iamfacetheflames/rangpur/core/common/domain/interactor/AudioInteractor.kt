package io.github.iamfacetheflames.rangpur.core.common.domain.interactor

import io.github.iamfacetheflames.rangpur.core.common.domain.di.DomainDi
import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.data.Directory

class AudioInteractor(
    private val domainDi: DomainDi,
) {

    private val directoryInteractor by lazy {
        domainDi.get(DirectoryInteractor::class)
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