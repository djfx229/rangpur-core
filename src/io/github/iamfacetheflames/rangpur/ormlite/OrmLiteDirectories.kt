package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import io.github.iamfacetheflames.rangpur.data.Directory
import io.github.iamfacetheflames.rangpur.repository.database.Database
import java.sql.SQLException

class OrmLiteDirectories(var source: ConnectionSource) : Database.Directories {

    override fun getOnlyRoot(): List<Directory> {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.where().isNull("parent_id")
        queryBuilder.orderByRaw("name COLLATE NOCASE")
        val preparedQuery = queryBuilder.prepare()
        return dao.query(preparedQuery)
    }

    override fun getFrom(parent: Directory): List<Directory> {
        val dao = DaoManager.createDao(source, OrmLiteDirectory::class.java)
        val queryBuilder = dao.queryBuilder()
        queryBuilder.where().eq("parent_id", parent)
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

}