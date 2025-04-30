package io.github.djfx229.rangpur.ormlite.repository.database

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.dao.GenericRawResults
import com.j256.ormlite.support.ConnectionSource
import io.github.djfx229.rangpur.feature.playlist.domain.model.Playlist
import io.github.djfx229.rangpur.feature.playlist.domain.model.PlaylistFolder
import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.ormlite.data.OrmLiteAudioInPlaylist
import io.github.djfx229.rangpur.ormlite.data.OrmLitePlaylist
import java.sql.SQLException

class OrmLitePlaylists(val source: ConnectionSource) : Database.Playlists {

    override fun create(playlist: Playlist) {
        return update(playlist)
    }

    override fun create(items: List<Playlist>) {
        val dao = DaoManager.createDao(source, OrmLitePlaylist::class.java)
        dao.callBatchTasks {
            for (playlist in items) {
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

    override fun getAll(): List<Playlist> {
        return getFrom(null)
    }

    override fun update(playlist: Playlist) {
        val dao = DaoManager.createDao(source, OrmLitePlaylist::class.java)
        dao.createOrUpdate(playlist as OrmLitePlaylist)
    }

    override fun delete(playlist: Playlist) {
        val daoPlaylist = DaoManager.createDao(source, OrmLitePlaylist::class.java)
        val daoAudioInPlaylist = DaoManager.createDao(source, OrmLiteAudioInPlaylist::class.java)
        val request = "DELETE FROM audio_in_playlist WHERE playlist_uuid = ?;"
        daoAudioInPlaylist.executeRaw(request, playlist.uuid)
        daoPlaylist.delete(playlist as OrmLitePlaylist)
    }

    override fun getFrom(playlistFolder: PlaylistFolder?): List<Playlist> {
        val dao = DaoManager.createDao(source, OrmLitePlaylist::class.java)
        if (playlistFolder == null) {
            val queryBuilder = dao.queryBuilder()
            queryBuilder.orderBy("timestamp_created", false)
            val preparedQuery = queryBuilder.prepare()
            return dao.query(preparedQuery)
        } else {
            val query = "SELECT * FROM playlist " +
                    "WHERE folder_uuid = ? " +
                    "ORDER BY timestamp_created DESC;"
            val queryResult: GenericRawResults<OrmLitePlaylist> = dao.queryRaw(
                query,
                dao.rawRowMapper,
                playlistFolder?.uuid.toString()
            )
            return queryResult.results
        }
    }

}
