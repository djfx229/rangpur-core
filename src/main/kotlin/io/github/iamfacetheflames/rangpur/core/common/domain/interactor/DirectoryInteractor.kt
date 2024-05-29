package io.github.iamfacetheflames.rangpur.core.common.domain.interactor

import io.github.iamfacetheflames.rangpur.core.common.domain.di.DomainDi
import io.github.iamfacetheflames.rangpur.core.common.domain.model.CoreConfig
import io.github.iamfacetheflames.rangpur.core.common.domain.repository.ConfigRepository
import io.github.iamfacetheflames.rangpur.core.data.Directory
import io.github.iamfacetheflames.rangpur.core.repository.database.Database

class DirectoryInteractor(
    private val domainDi: DomainDi,
) {

    private val configRepository: ConfigRepository<CoreConfig> by lazy {
        domainDi.getConfigRepository(CoreConfig::class)
    }

    private val databaseDirectories by lazy {
        domainDi.get(Database::class).directories
    }

    fun getFullPath(directory: Directory): String {
        // не нужно добавлять разделитель - locationInMusicDirectory всегда начинается со слэша
        return configRepository.get().musicLibraryPath + directory.locationInMusicDirectory
    }

    fun getFullPath(directoryUUID: String, cacheDirectories: HashMap<String, Directory>? = null): String {
        val cachedDirectory = cacheDirectories?.get(directoryUUID)
        val directory = if (cachedDirectory == null) {
            val directoryFromDB = databaseDirectories.getItem(directoryUUID)
            cacheDirectories?.put(directoryUUID, directoryFromDB)
            directoryFromDB
        } else {
            cachedDirectory
        }
        return getFullPath(directory)
    }

}