package io.github.djfx229.rangpur.ormlite.repository.database

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import io.github.iamfacetheflames.rangpur.core.data.*
import io.github.iamfacetheflames.rangpur.core.feature.library.data.repository.LibraryRepositoryImpl
import io.github.iamfacetheflames.rangpur.core.feature.library.domain.repository.LibraryRepository
import io.github.iamfacetheflames.rangpur.core.repository.database.Database
import io.github.iamfacetheflames.rangpur.ormlite.data.*
import java.sql.Date

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
                this.directoryUUID = directory.uuid
                this.fileName = fileName
                this.artist = artist
                this.title = title
                this.dateCreated = dateCreated.toString()
                this.timestampCreated = dateCreated.time
                this.duration = 0
            }
        }

        override fun createEmptyAudio(): Audio {
            return OrmLiteAudio()
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
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.executeRawNoArgs(
            """
                CREATE TABLE IF NOT EXISTS "rangpur_info" (
                    "tag"	TEXT NOT NULL UNIQUE,
                    "value"	TEXT
                );
            """
        )
        try {
            dao.executeRawNoArgs(
                """
                    INSERT INTO "rangpur_info" ("tag","value") VALUES ('database_version','1');
                """
            )
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun getBuilder(): Database.Builder = builder

    override val directories: Database.Directories = OrmLiteDirectories(source)

    override val calendar: Database.Calendar = OrmLiteCalendar(source)

    override val audios: Database.Audios = OrmLiteAudios(source)

    override val playlistFolders: Database.PlaylistFolders = OrmLitePlaylistFolders(source)

    override val playlists: Database.Playlists = OrmLitePlaylists(source)

    override val playlistWithAudios: Database.PlaylistWithAudios = OrmLitePlaylistWithAudios(source)

    override val library: LibraryRepository = LibraryRepositoryImpl(source)

}
