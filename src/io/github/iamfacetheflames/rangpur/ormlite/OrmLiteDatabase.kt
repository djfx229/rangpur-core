package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.dao.GenericRawResults
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import io.github.iamfacetheflames.rangpur.repository.Database
import io.github.iamfacetheflames.rangpur.data.*
import java.lang.StringBuilder
import java.sql.Date
import java.sql.SQLException

class OrmLiteDatabase(var source: ConnectionSource): Database {

    private var builder = object : Database.DaoBuilder {

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
                this.ormFolder = folder as OrmLitePlaylistFolder?
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

    override fun getBuilder(): Database.DaoBuilder = builder

    override fun saveDirectories(directories: List<Directory>) {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        dao.callBatchTasks {
            for (directory in directories) {
                try {
                    dao.createOrUpdate(directory as OrmLiteDirectory)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getRootDirectories(): List<Directory> {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.where().isNull("parent_id")
        queryBuilder.orderByRaw("name COLLATE NOCASE")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun getDirectories(): List<Directory> {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.orderByRaw("name COLLATE NOCASE")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun getDirectories(parent: Directory): List<Directory> {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.where().eq("parent_id", parent)
        queryBuilder.orderByRaw("name COLLATE NOCASE")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun getDirectory(directoryId: Long): Directory? {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.where().eq("id", directoryId)
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery).first()
    }

    override fun getDateList(): List<String> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val results = ArrayList<String>()
        val request = "SELECT date_created FROM audio GROUP BY date_created ORDER BY timestamp_created DESC"
        dao.queryRaw(request).results.forEach { results.add( it.first() ) }
        return results
    }

    override fun getYears(): List<String> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val results = ArrayList<String>()
        val request = "SELECT strftime('%Y', date_created) as t1 FROM audio GROUP BY t1 ORDER BY timestamp_created DESC"
        dao.queryRaw(request).results.forEach { results.add( it.first() ) }
        return results
    }

    override fun getMonths(year: String): List<String> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val results = ArrayList<String>()
        AudioField.apply {
            val request = "SELECT strftime('%Y.%m', $DATE_CREATED) as t1 " +
                    "FROM $AUDIO_TABLE_NAME WHERE $DATE_CREATED LIKE '$year%' " +
                    "GROUP BY t1 ORDER BY $TIMESTAMP_CREATED DESC"
            dao.queryRaw(request).results.forEach { results.add( it.first() ) }
            return results
        }
    }

    override fun getDays(yearAndMonth: String): List<String> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val results = ArrayList<String>()
        AudioField.apply {
            val request = "SELECT strftime('%Y.%m.%d', $DATE_CREATED) as t1 " +
                    "FROM $AUDIO_TABLE_NAME WHERE $DATE_CREATED LIKE '$yearAndMonth%' " +
                    "GROUP BY t1 ORDER BY $TIMESTAMP_CREATED DESC"
            dao.queryRaw(request).results.forEach { results.add( it.first() ) }
            return results
        }
    }

    override fun updateAudios(audios: List<Audio>) {
        val dao: Dao<OrmLiteAudio, String> =
            DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.callBatchTasks {
            for (audio in audios) {
                dao.update(audio as OrmLiteAudio)
            }
        }
    }

    override fun saveAudios(audios: List<Audio>) {
        val dao: Dao<OrmLiteAudio, String> =
            DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.callBatchTasks {
            for (audio in audios) {
                try {
                    dao.createIfNotExists(audio as OrmLiteAudio)
                } catch (e: SQLException) {
                    if (e.cause?.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true) {
                        // ignore
                    } else {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun getAudios(): List<Audio> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val request = "SELECT * FROM audio ORDER BY ${AudioField.TIMESTAMP_CREATED} DESC;"
        val queryResult: GenericRawResults<OrmLiteAudio> = dao.queryRaw(request, dao.rawRowMapper)
        return queryResult.results
    }

    override fun getAudios(filter: Filter): List<Audio> {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val request = StringBuilder().apply {
            append("SELECT a.* FROM audio as a INNER JOIN directory as p ON a.directory_id = p.id ")
            AudioField.apply {
                val conditions = ArrayList<String>().apply {
                    if (filter.isSearchRequest()) {
                        val item: Pair<Int, Keys.Key>? = Keys.keyMap.filter { it.value.lancelot.equals(filter.searchRequest, true) }.toList().firstOrNull()
                        if (item != null) {
                            add(
                                "(" + like(FILE_NAME, filter.searchRequest) + " OR $KEY = ${item.first} ) "
                            )
                        } else {
                            add(
                                like(FILE_NAME, filter.searchRequest)
                            )
                        }
                    }
                    if (filter.isDirectoriesFiltered()) {
                        val locationDirs = mutableListOf<String>()
                        filter.directories.forEach {
                            it.locationInMusicDirectory?.let { location ->
                                locationDirs.add(location)
                            }
                        }
                        add(
                            likeOrExpression("p.location", locationDirs, true)
                        )
                    }
                    if (filter.isDateFiltered()) {
                        add(
                            likeOrExpression(DATE_CREATED, filter.dateList)
                        )
                    }
                    if (filter.isOnlyWithoutPlaylist) {
                        add("(SELECT COUNT(*) FROM audio_in_playlist AS aip WHERE aip.audio_id = a.id) == 0 ")
                    }
                }
                append(where(conditions))
                filter.sort.apply {
                    append("ORDER BY $columnName $direction ")
                }
                append(";")
            }
        }.toString()
        val queryResult: GenericRawResults<OrmLiteAudio> = dao.queryRaw(request, dao.rawRowMapper)
        return queryResult.results
    }

    override fun deleteAudios(audios: List<Audio>) {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.delete(audios as List<OrmLiteAudio>)
    }

    override fun createOrUpdatePlaylist(playlist: Playlist) {
        val dao = DaoManager.createDao(source, OrmLitePlaylist::class.java)
        dao.createOrUpdate(playlist as OrmLitePlaylist)
    }

    override fun removePlaylist(playlist: Playlist) {
        val daoPlaylist = DaoManager.createDao(source, OrmLitePlaylist::class.java)
        val daoAudioInPlaylist = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        val request = "DELETE FROM audio_in_playlist WHERE playlist_id = ?;"
        daoAudioInPlaylist.executeRaw(request, playlist.id.toString())
        daoPlaylist.delete(playlist as OrmLitePlaylist)
    }

    override fun removePlaylistFolder(playlistFolder: PlaylistFolder) {
        val dao = DaoManager.createDao(source, OrmLitePlaylistFolder::class.java)
        dao.delete(playlistFolder as OrmLitePlaylistFolder)
    }

    override fun getPlaylists(): List<Playlist> = getPlaylists(null)

    override fun getPlaylists(playlistFolder: PlaylistFolder?): List<Playlist> {
        val dao = DaoManager.createDao(source, OrmLitePlaylist::class.java)
        if (playlistFolder == null) {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.orderBy("timestamp_created", false)
            val preparedQuery = queryBuilder.prepare()
            return dao.query(preparedQuery)
        } else {
            val query = "SELECT * FROM playlist " +
                    "WHERE folder_id = ? " +
                    "ORDER BY timestamp_created DESC;"
            val queryResult: GenericRawResults<OrmLitePlaylist> = dao.queryRaw(
                query,
                dao.rawRowMapper,
                playlistFolder?.id.toString()
            )
            return queryResult.results
        }
    }

    override fun savePlaylists(playlists: List<Playlist>) {
        val dao = DaoManager.createDao(source, OrmLitePlaylist::class.java)
        dao.callBatchTasks {
            for (playlist in playlists) {
                try {
                    dao.createOrUpdate(playlist as OrmLitePlaylist)
                } catch (e: SQLException) {
                    if (e.cause?.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true) {
                        // ignore
                    } else {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun getPlaylistAudios(): List<AudioInPlaylist> = getPlaylistAudios(null)

    override fun getPlaylistAudios(playlist: Playlist?): List<AudioInPlaylist> {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        val queryBuilder = dao.queryBuilder()
        if (playlist != null) {
            queryBuilder.where().eq("playlist_id", playlist)
        }
        queryBuilder.orderByRaw("position ASC")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun addAudiosInPlaylist(audios: List<Audio>, playlistId: Long) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            val request = "INSERT INTO audio_in_playlist (audio_id, playlist_id, position) \n" +
                    "VALUES (?, ?, (SELECT ifnull(MAX(position), 0)+1 FROM audio_in_playlist WHERE playlist_id = ?));"
            audios.forEachIndexed { index, audio ->
                dao.executeRaw(request, audio.id.toString(), playlistId.toString(), playlistId.toString())
            }
        }
    }

    override fun deleteAudiosFromPlaylist(audios: List<AudioInPlaylist>, playlistId: Long) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            val request = "DELETE FROM audio_in_playlist WHERE id = ?;"
            audios.forEachIndexed { index, audio ->
                dao.executeRaw(request, audio.id.toString())
            }
        }
    }

    override fun moveAudiosInPlaylistToNewPosition(audios: List<AudioInPlaylist>) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            val request = "UPDATE audio_in_playlist SET position = ? WHERE id = ?;"
            audios.forEachIndexed { index, audio ->
                dao.updateRaw(request, (index + 1).toString(), audio.id.toString())
            }
        }
    }

    override fun savePlaylistAudios(audios: List<AudioInPlaylist>) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            for (audio in audios) {
                try {
                    dao.createOrUpdate(audio as OrmLiteAudioInPlaylist)
                } catch (e: SQLException) {
                    if (e.cause?.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true) {
                        // ignore
                    } else {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun getPlaylistFolders(): List<PlaylistFolder> {
        val dao = DaoManager.createDao(source, OrmLitePlaylistFolder::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.orderBy("timestamp_created", false)
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun createOrUpdatePlaylistFolder(playlistFolder: PlaylistFolder) {
        val dao = DaoManager.createDao(source, OrmLitePlaylistFolder::class.java)
        dao.createOrUpdate(playlistFolder as OrmLitePlaylistFolder)
    }

    override fun savePlaylistFolders(folders: List<PlaylistFolder>) {
        val dao = DaoManager.createDao(source, OrmLitePlaylistFolder::class.java)
        dao.callBatchTasks {
            for (folder in folders) {
                try {
                    dao.createOrUpdate(folder as OrmLitePlaylistFolder)
                } catch (e: SQLException) {
                    if (e.cause?.message?.contains("SQLITE_CONSTRAINT_UNIQUE") == true) {
                        // ignore
                    } else {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun where(conditions: List<String>): String =
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

    private fun likeOrExpression(field: String, values: List<String>, startsWith: Boolean = false): String =
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

    private fun like(field: String, value: String, startsWith: Boolean = false): String {
        return if (startsWith) {
            "$field LIKE \"$value%\" "
        } else {
            "$field LIKE \"%$value%\" "
        }
    }

    private fun inIds(field: String, array: List<WithId>): String {
        return if (array.isNotEmpty()) {
            val arrayString = StringBuilder().apply {
                append("(")
                for ((index, item) in array.withIndex()) {
                    val id = item.id.toString()
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

}