package io.github.iamfacetheflames.rangpur.model

import io.github.iamfacetheflames.rangpur.repository.database.Database
import io.github.iamfacetheflames.rangpur.data.Audio
import io.github.iamfacetheflames.rangpur.data.AudioInPlaylist
import io.github.iamfacetheflames.rangpur.data.Filter
import io.github.iamfacetheflames.rangpur.data.Playlist

class AudioLibraryModel(val database: Database) {

    fun getAllAudios() = database.getAudios()

    fun getAudios(filter: Filter): List<Audio> {
        return database.getAudios(filter)
    }

    fun getAudios(playlist: Playlist): List<AudioInPlaylist> {
        return database.getPlaylistAudios(playlist)
    }

}