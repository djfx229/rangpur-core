package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import io.github.iamfacetheflames.rangpur.data.*
import io.github.iamfacetheflames.rangpur.repository.database.Database
import java.sql.Date
import java.sql.SQLException

class OrmLiteDatabase(var source: ConnectionSource): Database {

    private var builder = object : Database.Builder {

        override fun createAudio(
            directory: Directory,
            fileName: String,
            artist: String,
            title: String,
            dateCreated: Date
        ): Audio {
            return OrmLiteAudio().apply {
                this.directory = directory
                this.fileName = fileName
                this.artist = artist
                this.title = title
                this.dateCreated = dateCreated.toString()
                this.timestampCreated = dateCreated.time
                this.duration = 0
            }
        }

        override fun createPlaylist(name: String, folder: PlaylistFolder?): Playlist {
            return OrmLitePlaylist().apply {
                this.name = name
                this.timestampCreated = java.util.Date().time
                this.folder = folder
            }
        }

        override fun createPlaylistFolder(name: String, parent: PlaylistFolder?): PlaylistFolder {
            return OrmLitePlaylistFolder().apply {
                this.name = name
                this.timestampCreated = java.util.Date().time
                this.ormParent = parent as OrmLitePlaylistFolder?
            }
        }

        override fun createDirectory(path: String,
                                     location: String,
                                     parent: Directory?): Directory {
            return OrmLiteDirectory().apply {
                this.name = path
                this.locationInMusicDirectory = location
                this.parent = parent
            }
        }

        override fun audioInPlaylist(audio: Audio, playlist: Playlist): AudioInPlaylist {
            return OrmLiteAudioInPlaylist().apply {
                this.ormAudio = audio as OrmLiteAudio
                this.ormPlaylist = playlist as OrmLitePlaylist
            }
        }

    }

    init {
        TableUtils.createTableIfNotExists(source, OrmLiteDirectory::class.java)
        TableUtils.createTableIfNotExists(source, OrmLiteAudio::class.java)
        TableUtils.createTableIfNotExists(source, OrmLitePlaylistFolder::class.java)
        TableUtils.createTableIfNotExists(source, OrmLitePlaylist::class.java)
        TableUtils.createTableIfNotExists(source, OrmLiteAudioInPlaylist::class.java)
    }

    override fun getBuilder(): Database.Builder = builder

    override val directories: Database.Directories = OrmLiteDirectories(source)

    override fun saveDirectories(items: List<Directory>) = directories.update(items)
    override fun getRootDirectories(): List<Directory> = directories.getOnlyRoot()
    override fun getDirectories(): List<Directory> = directories.getAll()
    override fun getDirectories(parent: Directory): List<Directory> = directories.getFrom(parent)
    override fun getDirectory(directoryUUID: String): Directory? = directories.getItem(directoryUUID)

    override val calendar: Database.Calendar = OrmLiteCalendar(source)

    override val audios: Database.Audios = OrmLiteAudios(source)

    override fun updateAudios(items: List<Audio>) = audios.update(items)
    override fun saveAudios(items: List<Audio>) = audios.create(items)
    override fun getAudios(): List<Audio> = audios.getAll()
    override fun getAudios(filter: Filter): List<Audio> = audios.getFiltered(filter)
    override fun deleteAudios(items: List<Audio>) = audios.delete(items)

    override val playlistFolders: Database.PlaylistFolders = OrmLitePlaylistFolders(source)

    override fun getPlaylistFolders(): List<PlaylistFolder> = playlistFolders.getAll()
    override fun createOrUpdatePlaylistFolder(playlistFolder: PlaylistFolder) = playlistFolders.update(playlistFolder)
    override fun savePlaylistFolders(folders: List<PlaylistFolder>) = playlistFolders.create(folders)
    override fun removePlaylistFolder(playlistFolder: PlaylistFolder) = playlistFolders.delete(playlistFolder)

    override val playlists: Database.Playlists = OrmLitePlaylists(source)

    override fun createOrUpdatePlaylist(playlist: Playlist) = playlists.update(playlist)
    override fun removePlaylist(playlist: Playlist) = playlists.delete(playlist)
    override fun getPlaylists(): List<Playlist> = playlists.getAll()
    override fun getPlaylists(playlistFolder: PlaylistFolder?): List<Playlist> = playlists.getFrom(playlistFolder)
    override fun savePlaylists(items: List<Playlist>) = playlists.create(items)

    override val playlistWithAudios: Database.PlaylistWithAudios = OrmLitePlaylistWithAudios(source)

    override fun getPlaylistAudios(): List<AudioInPlaylist> = playlistWithAudios.getAll()
    override fun getPlaylistAudios(playlist: Playlist?): List<AudioInPlaylist> = playlistWithAudios.getFrom(playlist)
    override fun addAudiosInPlaylist(audios: List<Audio>, playlistUUID: String) =
        playlistWithAudios.create(audios, playlistUUID)
    override fun deleteAudiosFromPlaylist(audios: List<AudioInPlaylist>, playlistUUID: String) =
        playlistWithAudios.delete(audios, playlistUUID)
    override fun moveAudiosInPlaylistToNewPosition(audios: List<AudioInPlaylist>, newPosition: Int) =
        playlistWithAudios.changePosition(audios, newPosition)
    override fun savePlaylistAudios(audios: List<AudioInPlaylist>) = playlistWithAudios.create(audios)

}

fun where(conditions: List<String>): String =
    StringBuilder().apply {
        if (conditions.isNotEmpty()) {
            append("WHERE ")
            for ((index, item) in conditions.withIndex()) {
                append(
                    if (index == 0) {
                        "$item "
                    } else {
                        "AND $item "
                    }
                )
            }
        }
        append(" ")
    }.toString()

fun likeOrExpression(field: String, values: List<String>, startsWith: Boolean = false): String =
    StringBuilder().apply {
        if (values.isNotEmpty()) {
            append("(")
            for ((index, item) in values.withIndex()) {
                val value = like(field, item, startsWith)
                append(
                    if (index == 0) {
                        "$value "
                    } else {
                        "OR $value "
                    }
                )
            }
            append(")")
        }
        append(" ")
    }.toString()

fun like(field: String, value: String, startsWith: Boolean = false): String {
    return if (startsWith) {
        "$field LIKE \"$value%\" "
    } else {
        "$field LIKE \"%$value%\" "
    }
}

fun inIds(field: String, array: List<WithId>): String {
    return if (array.isNotEmpty()) {
        val arrayString = StringBuilder().apply {
            append("(")
            for ((index, item) in array.withIndex()) {
                val id = item.uuid
                append(
                    if (index == 0) {
                        id
                    } else {
                        ", $id"
                    }
                )
            }
            append(")")
        }.toString()
        "$field IN $arrayString "
    } else {
        " "
    }
}