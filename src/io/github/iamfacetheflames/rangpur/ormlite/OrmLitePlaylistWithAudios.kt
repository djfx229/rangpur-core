package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import io.github.iamfacetheflames.rangpur.data.*
import io.github.iamfacetheflames.rangpur.repository.database.Database
import java.sql.SQLException

class OrmLitePlaylistWithAudios(val source: ConnectionSource): Database.PlaylistWithAudios {

    override fun getFrom(playlist: Playlist?): List<AudioInPlaylist> {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        val queryBuilder = dao.queryBuilder()
        if (playlist != null) {
            queryBuilder.where().eq("playlist_uuid", playlist)
        }
        queryBuilder.orderByRaw("position ASC")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun create(items: List<Audio>, playlistUUID: String) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            val request = "INSERT INTO audio_in_playlist (audio_uuid, playlist_uuid, position) \n" +
                    "VALUES (?, ?, (SELECT ifnull(MAX(position), 0)+1 FROM audio_in_playlist WHERE playlist_uuid = ?));"
            items.forEachIndexed { index, audio ->
                dao.executeRaw(request, audio.uuid, playlistUUID, playlistUUID)
            }
        }
    }

    override fun create(items: List<AudioInPlaylist>) {
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

    override fun changePosition(items: List<AudioInPlaylist>, newPosition: Int) {
        val dao = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        dao.callBatchTasks {
            val request = "UPDATE audio_in_playlist SET position = ? WHERE uuid = ?;"
            items.forEachIndexed { index, audio ->
                dao.updateRaw(request, (index + 1).toString(), audio.uuid)
            }
        }
    }

    override fun getAll(): List<AudioInPlaylist> = getFrom(null)

}
