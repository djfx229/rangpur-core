package io.github.iamfacetheflames.rangpur.core.model

import io.github.iamfacetheflames.rangpur.core.repository.database.Database
import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.data.AudioInPlaylist
import io.github.iamfacetheflames.rangpur.core.data.Playlist
import io.github.iamfacetheflames.rangpur.core.data.PlaylistFolder
import java.io.File
import java.util.*

class PlaylistLibraryModel(private val database: Database) {

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

    fun moveAudiosInPlaylistToNewPosition(
        fullList: List<AudioInPlaylist>,
        selectedList: List<AudioInPlaylist>,
        movePosition: Int
    ): List<AudioInPlaylist> {
        val isUp = movePosition < selectedList.first().position
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
            if (isUp) {
                movePosition
            } else {
                movePosition + 1
            },
            selectedList
        )
        val finalList = nullableResultList.filterNotNull()
        database.playlistWithAudios.changePosition(finalList)
        return finalList
    }

    fun exportPlaylistToTextFile(
        file: File,
        playlist: Playlist
    ) {
        val audios = database.getPlaylistAudios(playlist)
        PlaylistToFile.exportPlaylistTxt(file.name, file.parentFile.absolutePath, audios)
    }

}