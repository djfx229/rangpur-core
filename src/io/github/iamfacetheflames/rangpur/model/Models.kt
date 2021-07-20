package io.github.iamfacetheflames.rangpur.model

import io.github.iamfacetheflames.rangpur.repository.Configuration
import io.github.iamfacetheflames.rangpur.repository.database.Database

class Models(val database: Database, val config: Configuration) {

    val filterLibrary = FilterLibraryModel(database)
    val audioLibrary = AudioLibraryModel(database)
    val playlistLibrary = PlaylistLibraryModel(database)
    val sync = SyncModel(database, config)

}