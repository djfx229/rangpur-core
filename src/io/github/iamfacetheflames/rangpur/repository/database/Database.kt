package io.github.iamfacetheflames.rangpur.repository.database

import io.github.iamfacetheflames.rangpur.data.*
import java.sql.Date

interface Database {

    interface Calendar {
        fun getDateList(): List<String>
        fun getYears(): List<String>
        fun getMonths(year: String): List<String>
        fun getDays(yearAndMonth: String): List<String>
    }

    interface Directories: MultipleCRUD<Directory>, ItemGetter<Directory> {
        fun getOnlyRoot(): List<Directory>
        fun getFrom(parent: Directory): List<Directory>
    }

    interface Audios: MultipleCRUD<Audio> {
        fun getFiltered(filter: Filter): List<Audio>
    }

    interface PlaylistFolders : AllGetter<PlaylistFolder> {
        fun create(playlistFolder: PlaylistFolder)
        fun create(folders: List<PlaylistFolder>)
        fun update(playlistFolder: PlaylistFolder)
        fun delete(playlistFolder: PlaylistFolder)
    }

    interface Playlists : AllGetter<Playlist> {
        fun create(playlist: Playlist)
        fun create(items: List<Playlist>)
        fun update(playlist: Playlist)
        fun delete(playlist: Playlist)
        fun getFrom(playlistFolder: PlaylistFolder?): List<Playlist>
    }

    interface PlaylistWithAudios : AllGetter<AudioInPlaylist> {
        fun getFrom(playlist: Playlist?): List<AudioInPlaylist>
        fun create(items: List<Audio>, playlistUUID: String)
        fun create(items: List<AudioInPlaylist>)
        fun delete(items: List<AudioInPlaylist>, playlistUUID: String)
        fun changePosition(items: List<AudioInPlaylist>)
    }

    interface Builder {
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

    val directories: Directories
    fun saveDirectories(directories: List<Directory>)
    fun getRootDirectories(): List<Directory>
    fun getDirectories(): List<Directory>
    fun getDirectory(directoryUUID: String): Directory?
    fun getDirectories(parent: Directory): List<Directory>

    val calendar: Calendar

    val audios: Audios
    fun updateAudios(audios: List<Audio>)
    fun saveAudios(audios: List<Audio>)
    fun getAudios(): List<Audio>
    fun getAudios(filter: Filter): List<Audio>
    fun deleteAudios(audios: List<Audio>)

    val playlistFolders: PlaylistFolders
    fun getPlaylistFolders(): List<PlaylistFolder>
    fun createOrUpdatePlaylistFolder(playlistFolder: PlaylistFolder)
    fun savePlaylistFolders(folders: List<PlaylistFolder>)
    fun removePlaylistFolder(playlistFolder: PlaylistFolder)

    val playlists: Playlists
    fun createOrUpdatePlaylist(playlist: Playlist)
    fun savePlaylists(playlists: List<Playlist>)
    fun removePlaylist(playlist: Playlist)
    fun getPlaylists(): List<Playlist>
    fun getPlaylists(playlistFolder: PlaylistFolder?): List<Playlist>

    val playlistWithAudios: PlaylistWithAudios
    @Deprecated("Please use playlistWithAudios.getAll() instead.")
    fun getPlaylistAudios(): List<AudioInPlaylist>
    @Deprecated("Please use playlistWithAudios.getFrom(playlist) instead.")
    fun getPlaylistAudios(playlist: Playlist?): List<AudioInPlaylist>
    @Deprecated("Please use playlistWithAudios.create(audios, playlistUUID) instead.")
    fun addAudiosInPlaylist(audios: List<Audio>, playlistUUID: String)
    @Deprecated("Please use deleteAudiosFromPlaylist(audios: List<AudioInPlaylist>, playlistUUID: String) instead.")
    fun deleteAudiosFromPlaylist(items: List<AudioInPlaylist>, playlistUUID: String)
    @Deprecated("Please use playlistWithAudios.changePosition(audios, newPosition) instead.")
    fun moveAudiosInPlaylistToNewPosition(audios: List<AudioInPlaylist>)
    @Deprecated("Please use playlistWithAudios.create(audios) instead.")
    fun savePlaylistAudios(audios: List<AudioInPlaylist>)

    fun getBuilder(): Builder

}