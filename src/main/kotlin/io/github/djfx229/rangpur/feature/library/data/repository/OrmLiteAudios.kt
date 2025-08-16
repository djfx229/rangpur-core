package io.github.djfx229.rangpur.feature.library.data.repository

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.dao.GenericRawResults
import com.j256.ormlite.support.ConnectionSource
import io.github.djfx229.rangpur.common.data.database.AudioField
import io.github.djfx229.rangpur.common.domain.database.Database
import io.github.djfx229.rangpur.feature.library.data.entity.OrmLiteAudio
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import java.sql.SQLException

class OrmLiteAudios(var source: ConnectionSource) : Database.Audios {

    private var currentAudiosRequest: GenericRawResults<OrmLiteAudio>? = null

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