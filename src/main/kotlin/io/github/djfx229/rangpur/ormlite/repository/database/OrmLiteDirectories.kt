package io.github.djfx229.rangpur.ormlite.repository.database

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.dao.GenericRawResults
import com.j256.ormlite.support.ConnectionSource
import io.github.djfx229.rangpur.core.data.Directory
import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.ormlite.data.OrmLiteDirectory
import java.sql.SQLException

class OrmLiteDirectories(var source: ConnectionSource) : Database.Directories {

    override fun getOnlyRoot(): List<Directory> {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.where().isNull("parent_uuid")
        queryBuilder.orderByRaw("name COLLATE NOCASE")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun getFrom(parent: Directory): List<Directory> {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.where().eq("parent_uuid", parent)
        queryBuilder.orderByRaw("name COLLATE NOCASE")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun create(items: List<Directory>) {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        dao.callBatchTasks {
            for (directory in items) {
                try {
                    dao.createIfNotExists(directory as OrmLiteDirectory)
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

    override fun getAll(): List<Directory> {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.orderByRaw("name COLLATE NOCASE")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun update(items: List<Directory>) {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        dao.callBatchTasks {
            for (directory in items) {
                try {
                    dao.createOrUpdate(directory as OrmLiteDirectory)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun delete(items: List<Directory>) {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        dao.delete(items as List<OrmLiteDirectory>)
    }

    override fun getItem(uuid: String): Directory {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.where().eq("uuid", uuid)
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery).first()
    }

    override fun getItemByLocation(location: String): Directory? {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val request = "SELECT * FROM directory WHERE location = ? LIMIT 1;"
        val queryResult: GenericRawResults<OrmLiteDirectory> = dao.queryRaw(request, dao.rawRowMapper, location)
        return queryResult.results.firstOrNull()
    }

}