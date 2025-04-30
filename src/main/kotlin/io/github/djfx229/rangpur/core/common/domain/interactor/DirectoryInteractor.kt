package io.github.djfx229.rangpur.core.common.domain.interactor

import io.github.djfx229.rangpur.core.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.core.common.domain.model.CoreConfig
import io.github.djfx229.rangpur.core.common.domain.repository.ConfigRepository
import io.github.djfx229.rangpur.core.data.Directory
import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.core.common.domain.di.getConfigRepository

class DirectoryInteractor(
    private val di: DependencyInjector,
) {

    private val configRepository: ConfigRepository<CoreConfig> by lazy { di.getConfigRepository() }

    private val databaseDirectories by lazy {
        di.get<Database>().directories
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