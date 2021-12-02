package io.github.iamfacetheflames.rangpur.module.database

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
        fun getItemByLocation(location: String): Directory?
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

    @Deprecated("Please use directories.update(items) instead.")
    fun saveDirectories(directories: List<Directory>)
    @Deprecated("Please use directories.getOnlyRoot() instead.")
    fun getRootDirectories(): List<Directory>
    @Deprecated("Please use directories.getAll() instead.")
    fun getDirectories(): List<Directory>
    @Deprecated("Please use directories.getItem(directoryUUID) instead.")
    fun getDirectory(directoryUUID: String): Directory?
    @Deprecated("Please use directories.getFrom(parent) instead.")
    fun getDirectories(parent: Directory): List<Directory>

    val calendar: Calendar

    val audios: Audios
    @Deprecated("Please use audios.update(items) instead.")
    fun updateAudios(audios: List<Audio>)
    @Deprecated("Please use audios.create(items) instead.")
    fun saveAudios(audios: List<Audio>)
    @Deprecated("Please use audios.getAll() instead.")
    fun getAudios(): List<Audio>
    @Deprecated("Please use audios.getFiltered(filter) instead.")
    fun getAudios(filter: Filter): List<Audio>
    @Deprecated("Please use audios.delete(items) instead.")
    fun deleteAudios(audios: List<Audio>)

    val playlistFolders: PlaylistFolders
    @Deprecated("Please use playlistFolders.getAll() instead.")
    fun getPlaylistFolders(): List<PlaylistFolder>
    @Deprecated("Please use playlistFolders.update(playlistFolder) instead.")
    fun createOrUpdatePlaylistFolder(playlistFolder: PlaylistFolder)
    @Deprecated("Please use playlistFolders.create(folders) instead.")
    fun savePlaylistFolders(folders: List<PlaylistFolder>)
    @Deprecated("Please use playlistFolders.delete(playlistFolder) instead.")
    fun removePlaylistFolder(playlistFolder: PlaylistFolder)

    val playlists: Playlists
    @Deprecated("Please use playlists.update(playlist) instead.")
    fun createOrUpdatePlaylist(playlist: Playlist)
    @Deprecated("Please use playlists.create(items) instead.")
    fun savePlaylists(playlists: List<Playlist>)
    @Deprecated("Please use playlists.delete(playlist) instead.")
    fun removePlaylist(playlist: Playlist)
    @Deprecated("Please use playlists.getAll() instead.")
    fun getPlaylists(): List<Playlist>
    @Deprecated("Please use playlists.getFrom(playlistFolder) instead.")
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