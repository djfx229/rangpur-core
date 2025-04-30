package io.github.djfx229.rangpur.ormlite.repository.database

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import io.github.djfx229.rangpur.core.data.*
import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.ormlite.data.OrmLiteAudioInPlaylist
import java.sql.SQLException
import java.util.*

class OrmLitePlaylistWithAudios(val source: ConnectionSource): Database.PlaylistWithAudios {

    override fun getFrom(playlistUUID: String): List<AudioInPlaylist> {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.where().eq("playlist_uuid", playlistUUID)
        queryBuilder.orderByRaw("position ASC")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun create(items: List<Audio>, playlistUUID: String) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            val request = "INSERT INTO audio_in_playlist (uuid, audio_uuid, playlist_uuid, position) \n" +
                    "VALUES (?, ?, ?, (SELECT ifnull(MAX(position), 0)+1 FROM audio_in_playlist WHERE playlist_uuid = ?));"
            items.forEachIndexed { index, audio ->
                dao.executeRaw(request, UUID.randomUUID().toString(), audio.uuid, playlistUUID, playlistUUID)
            }
        }
    }

    override fun createWithCustomPosition(
        items: List<Audio>,
        playlistUUID: String,
        position: Int,
    ) {
        val innerPosition = position + 1
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)

        // делаем всё в рамках единой транзакции, если что-то пойдёт не так, то не получим ситуации с не консистентными данными
        dao.callBatchTasks {
            // смещаем треки находящиеся после позиции вставки на количество добавленных аудио
            val updatePositionsRequest = "UPDATE audio_in_playlist SET position = position + ? WHERE position > ? AND playlist_uuid = ?;"
            dao.updateRaw(
                updatePositionsRequest,
                (items.size).toString(),
                position.toString(),
                playlistUUID,
            )

            // добавляем новые треки в плейлист в пределах освободившегося окна
            val createRequest = "INSERT INTO audio_in_playlist (uuid, audio_uuid, playlist_uuid, position) \n" +
                    "VALUES (?, ?, ?, ?);"
            items.forEachIndexed { index, audio ->
                dao.executeRaw(
                    createRequest,
                    UUID.randomUUID().toString(),
                    audio.uuid,
                    playlistUUID,
                    (innerPosition + index).toString()
                )
            }
        }
    }

    override fun createOrUpdate(items: List<AudioInPlaylist>) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            for (audio in items) {
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

    override fun delete(items: List<AudioInPlaylist>, playlistUUID: String) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            val request = "DELETE FROM audio_in_playlist WHERE uuid = ?;"
            items.forEachIndexed { index, audio ->
                dao.executeRaw(request, audio.uuid)
            }
        }
    }

    override fun changePosition(items: List<AudioInPlaylist>) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            val request = "UPDATE audio_in_playlist SET position = ? WHERE uuid = ?;"
            items.forEachIndexed { index, audio ->
                dao.updateRaw(request, (index + 1).toString(), audio.uuid)
            }
        }
    }

    override fun getAll(): List<AudioInPlaylist> {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.orderByRaw("position ASC")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

}
