package io.github.djfx229.rangpur.ormlite.repository.database

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import io.github.djfx229.rangpur.core.data.PlaylistFolder
import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.ormlite.data.OrmLitePlaylistFolder
import java.sql.SQLException

class OrmLitePlaylistFolders(val source: ConnectionSource) : Database.PlaylistFolders {

    override fun create(playlistFolder: PlaylistFolder) {
        return update(playlistFolder)
    }

    override fun create(folders: List<PlaylistFolder>) {
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

    override fun update(playlistFolder: PlaylistFolder) {
        val dao = DaoManager.createDao(source, OrmLitePlaylistFolder::class.java)
        dao.createOrUpdate(playlistFolder as OrmLitePlaylistFolder)
    }

    override fun getAll(): List<PlaylistFolder> {
        val dao = DaoManager.createDao(source, OrmLitePlaylistFolder::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.orderBy("timestamp_created", false)
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun delete(playlistFolder: PlaylistFolder) {
        val dao = DaoManager.createDao(source, OrmLitePlaylistFolder::class.java)
        dao.delete(playlistFolder as OrmLitePlaylistFolder)
    }

}
