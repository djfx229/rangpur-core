package io.github.djfx229.rangpur.ormlite.repository.database

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.dao.GenericRawResults
import com.j256.ormlite.support.ConnectionSource
import io.github.djfx229.rangpur.common.data.database.SqliteRequestUtils
import io.github.djfx229.rangpur.core.data.*
import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.feature.library.domain.model.Keys
import io.github.djfx229.rangpur.ormlite.data.OrmLiteAudio
import java.lang.StringBuilder
import java.sql.SQLException

class OrmLiteAudios(var source: ConnectionSource) : Database.Audios {

    private var currentAudiosRequest: GenericRawResults<OrmLiteAudio>? = null

    override fun getFiltered(filter: Filter): List<Audio> {
        currentAudiosRequest?.close()
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val request = when (filter.mode) {
            Filter.Mode.LIBRARY -> getFilteredAudiosRequest(filter)
            Filter.Mode.PLAYLIST -> getAudiosFromPlaylistRequest(filter)
        }
        val queryResult: GenericRawResults<OrmLiteAudio> = if (filter.isSearchRequest()) {
            dao.queryRaw(request, dao.rawRowMapper, "%${filter.searchRequest}%")
        } else {
            dao.queryRaw(request, dao.rawRowMapper)
        }
        currentAudiosRequest = queryResult
        return queryResult.results
    }

    private fun getAudiosFromPlaylistRequest(filter: Filter): String {
        return StringBuilder().apply {
            append("SELECT a.* FROM audio as a INNER JOIN audio_in_playlist as p ON a.uuid = p.audio_uuid ")
            AudioField.apply {
                val conditions = ArrayList<String>().apply {
                    if (filter.isSearchRequest()) {
                        val item: Pair<Int, Keys.Key>? = Keys.keyMap.filter { it.value.lancelot.equals(filter.searchRequest, true) }.toList().firstOrNull()
                        if (item != null) {
                            add(
                                "( $FILE_NAME LIKE ? OR $KEY = ${item.first} ) "
                            )
                        } else {
                            add(
                                "$FILE_NAME LIKE ? "
                            )
                        }
                    }
                    add(
                        "p.playlist_uuid = '${filter.playlistUUID}' "
                    )
                }
                append(SqliteRequestUtils.where(conditions))
                append(SqliteRequestUtils.sortedBy(filter.sort))
                append(";")
            }
        }.toString()
    }

    private fun getFilteredAudiosRequest(filter: Filter): String {
        return StringBuilder().apply {
            append("SELECT a.* FROM audio as a INNER JOIN directory as p ON a.directory_uuid = p.uuid ")
            AudioField.apply {
                val conditions = ArrayList<String>().apply {
                    if (filter.isSearchRequest()) {
                        val item: Pair<Int, Keys.Key>? = Keys.keyMap.filter { it.value.lancelot.equals(filter.searchRequest, true) }.toList().firstOrNull()
                        if (item != null) {
                            add(
                                "( $FILE_NAME LIKE ? OR $KEY = ${item.first} ) "
                            )
                        } else {
                            add(
                                "$FILE_NAME LIKE ? "
                            )
                        }
                    }
                    if (filter.isDirectoriesFiltered()) {
                        val locationDirs = mutableListOf<String>()
                        filter.directories.forEach {
                            it.locationInMusicDirectory?.let(locationDirs::add)
                        }
                        add(
                            SqliteRequestUtils.likeOrExpression("p.location", locationDirs, true)
                        )
                    }
                    if (filter.isDateFiltered()) {
                        add(
                            SqliteRequestUtils.likeOrExpression(DATE_CREATED, filter.dateList)
                        )
                    }
                    if (filter.isOnlyWithoutPlaylist) {
                        add("(SELECT COUNT(*) FROM audio_in_playlist AS aip WHERE aip.audio_uuid = a.uuid) == 0 ")
                    }
                }
                append(SqliteRequestUtils.where(conditions))
                append(SqliteRequestUtils.sortedBy(filter.sort))
                append(";")
            }
        }.toString()
    }

    override fun create(items: List<Audio>) {
        val dao: Dao<OrmLiteAudio, String> =
            DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.callBatchTasks {
            for (audio in items) {
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

    override fun getAll(): List<Audio> {
        currentAudiosRequest?.close()
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        val request = "SELECT * FROM audio ORDER BY ${AudioField.TIMESTAMP_CREATED} DESC;"
        val queryResult: GenericRawResults<OrmLiteAudio> = dao.queryRaw(request, dao.rawRowMapper)
        currentAudiosRequest = queryResult
        return queryResult.results
    }

    override fun update(items: List<Audio>) {
        val dao: Dao<OrmLiteAudio, String> =
            DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.callBatchTasks {
            for (audio in items) {
                dao.update(audio as OrmLiteAudio)
            }
        }
    }

    override fun delete(items: List<Audio>) {
        val dao = DaoManager.createDao(source, OrmLiteAudio::class.java)
        dao.delete(items as List<OrmLiteAudio>)
    }

}