package io.github.iamfacetheflames.rangpur.model

import io.github.iamfacetheflames.rangpur.module.database.Database
import io.github.iamfacetheflames.rangpur.data.Audio
import io.github.iamfacetheflames.rangpur.data.AudioInPlaylist
import io.github.iamfacetheflames.rangpur.data.Playlist
import io.github.iamfacetheflames.rangpur.data.PlaylistFolder

class PlaylistLibraryModel(val database: Database) {

    fun createPlaylistFolder(name: String): PlaylistFolder {
        val playlist = database.getBuilder().createPlaylistFolder(name, null)
        database.createOrUpdatePlaylistFolder(playlist)
        return playlist
    }

    fun getPlaylistFolders(): List<PlaylistFolder> = database.getPlaylistFolders()

    fun createPlaylist(name: String, folder: PlaylistFolder?): Playlist {
        val playlist = database.getBuilder().createPlaylist(name, folder)
        database.createOrUpdatePlaylist(playlist)
        return playlist
    }

    fun renamePlaylist(name: String, playlist: Playlist) {
        playlist.name = name
        database.createOrUpdatePlaylist(playlist)
    }

    fun removePlaylist(playlist: Playlist) {
        database.removePlaylist(playlist)
    }

    fun removePlaylistFolder(folder: PlaylistFolder) {
        database.removePlaylistFolder(folder)
    }

    fun getPlaylists(playlistFolder: PlaylistFolder? = null): List<Playlist> = database.getPlaylists(playlistFolder)

    fun addAudiosInPlaylist(audios: List<Audio>, playlist: Playlist) {
        database.playlistWithAudios.create(audios, playlist.uuid)
    }

    fun deleteAudiosFromPlaylist(audios: List<AudioInPlaylist>, playlist: Playlist) {
        database.playlistWithAudios.delete(audios, playlist.uuid)
    }

}