package io.github.iamfacetheflames.rangpur.repository

import io.github.iamfacetheflames.rangpur.data.*
import java.sql.Date

interface Database {

    interface DaoBuilder {
        fun createDirectory(directoryName: String, location: String, parent: Directory? = null): Directory
        fun createAudio(
            directory: Directory,
            fileName: String,
            artist: String,
            title: String,
            dateCreated: Date): Audio
        fun createPlaylist(name: String, folder: PlaylistFolder?): Playlist
        fun createPlaylistFolder(name: String, parent: PlaylistFolder?): PlaylistFolder
        fun audioInPlaylist(audio: Audio, playlist: Playlist): AudioInPlaylist
    }

    fun getBuilder(): DaoBuilder

    fun saveDirectories(directories: List<Directory>)
    fun getRootDirectories(): List<Directory>
    fun getDirectories(): List<Directory>
    fun getDirectory(directoryId: Long): Directory?
    fun getDirectories(parent: Directory): List<Directory>

    fun getDateList(): List<String>
    fun getYears(): List<String>
    fun getMonths(year: String): List<String>
    fun getDays(yearAndMonth: String): List<String>

    fun updateAudios(audios: List<Audio>)
    fun saveAudios(audios: List<Audio>)
    fun getAudios(): List<Audio>
    fun getAudios(filter: Filter): List<Audio>
    fun deleteAudios(audios: List<Audio>)

    fun createOrUpdatePlaylist(playlist: Playlist)
    fun removePlaylist(playlist: Playlist)
    fun getPlaylists(): List<Playlist>
    fun getPlaylists(playlistFolder: PlaylistFolder?): List<Playlist>
    fun savePlaylists(playlists: List<Playlist>)

    fun getPlaylistAudios(): List<AudioInPlaylist>
    fun getPlaylistAudios(playlist: Playlist?): List<AudioInPlaylist>
    fun addAudiosInPlaylist(audios: List<Audio>, playlistId: Long)
    fun deleteAudiosFromPlaylist(audios: List<AudioInPlaylist>, playlistId: Long)
    fun moveAudiosInPlaylistToNewPosition(audios: List<AudioInPlaylist>)
    fun savePlaylistAudios(audios: List<AudioInPlaylist>)

    fun getPlaylistFolders(): List<PlaylistFolder>
    fun createOrUpdatePlaylistFolder(playlistFolder: PlaylistFolder)
    fun savePlaylistFolders(folders: List<PlaylistFolder>)
    fun removePlaylistFolder(playlistFolder: PlaylistFolder)

}