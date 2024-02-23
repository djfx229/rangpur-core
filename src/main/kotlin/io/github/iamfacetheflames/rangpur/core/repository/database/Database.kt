package io.github.iamfacetheflames.rangpur.core.repository.database

import io.github.iamfacetheflames.rangpur.core.data.*
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
        fun getFrom(playlistUUID: String): List<AudioInPlaylist>
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
        fun createEmptyAudio(): Audio
        fun createPlaylist(name: String, folder: PlaylistFolder?): Playlist
        fun createPlaylistFolder(name: String, parent: PlaylistFolder?): PlaylistFolder
        fun audioInPlaylist(audio: Audio, playlist: Playlist): AudioInPlaylist
    }

    val directories: Directories
    val calendar: Calendar
    val audios: Audios
    val playlistFolders: PlaylistFolders
    val playlists: Playlists
    val playlistWithAudios: PlaylistWithAudios

    fun getBuilder(): Builder

}