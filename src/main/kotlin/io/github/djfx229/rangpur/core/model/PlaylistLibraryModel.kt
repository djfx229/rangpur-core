package io.github.djfx229.rangpur.core.model

import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.feature.playlist.domain.model.AudioInPlaylist
import io.github.djfx229.rangpur.feature.playlist.domain.model.Playlist
import io.github.djfx229.rangpur.feature.playlist.domain.model.PlaylistFolder
import java.util.*

class PlaylistLibraryModel(private val database: Database) {

    fun createPlaylistFolder(name: String): PlaylistFolder {
        val playlist = database.getBuilder().createPlaylistFolder(name, null)
        database.playlistFolders.update(playlist)
        return playlist
    }

    fun getPlaylistFolders(): List<PlaylistFolder> = database.playlistFolders.getAll()

    fun createPlaylist(name: String, folder: PlaylistFolder?): Playlist {
        val playlist = database.getBuilder().createPlaylist(name, folder)
        database.playlists.update(playlist)
        return playlist
    }

    fun renamePlaylist(name: String, playlist: Playlist) {
        playlist.name = name
        database.playlists.update(playlist)
    }

    fun removePlaylist(playlist: Playlist) {
        database.playlists.delete(playlist)
    }

    fun removePlaylistFolder(folder: PlaylistFolder) {
        database.playlistFolders.delete(folder)
    }

    fun getPlaylists(playlistFolder: PlaylistFolder? = null): List<Playlist> = database.playlists.getFrom(playlistFolder)

    fun addAudiosInPlaylist(audios: List<Audio>, playlist: Playlist) {
        database.playlistWithAudios.create(audios, playlist.uuid)
    }

    fun deleteAudiosFromPlaylist(audios: List<AudioInPlaylist>, playlist: Playlist) {
        database.playlistWithAudios.delete(audios, playlist.uuid)
    }

    fun moveAudiosInPlaylistToNewPosition(
        fullList: List<AudioInPlaylist>,
        selectedList: List<AudioInPlaylist>,
        movePosition: Int
    ): List<AudioInPlaylist> {
        val nullableResultList = LinkedList<AudioInPlaylist?>()
        nullableResultList.addAll(
            fullList
        )
        selectedList.forEach {
            nullableResultList.set(
                nullableResultList.indexOf(it),
                null
            )
        }
        nullableResultList.addAll(
            movePosition,
            selectedList
        )
        val finalList = nullableResultList.filterNotNull()
        database.playlistWithAudios.changePosition(finalList)
        return finalList
    }

}
