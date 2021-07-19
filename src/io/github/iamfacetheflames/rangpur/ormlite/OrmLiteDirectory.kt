package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.github.iamfacetheflames.rangpur.data.Directory
import java.io.File

@DatabaseTable(tableName = "directory")
class OrmLiteDirectory : Directory {

    @DatabaseField(columnName = "name", canBeNull = false, uniqueIndexName = "unique_directory")
    override var name: String? = null

    @DatabaseField(columnName = "location", canBeNull = false, uniqueIndexName = "unique_location")
    override var locationInMusicDirectory: String? = null

    @DatabaseField(columnName = "id", generatedId = true)
    override var id: Long = 0

    override var parent: Directory?
        get() = ormParent
        set(value) {
            if (value != null) {
                ormParent = value as OrmLiteDirectory
            }
        }

    @DatabaseField(columnName = "parent_id", foreign = true, foreignAutoRefresh = true)
    var ormParent: OrmLiteDirectory? = null

    override fun toString(): String {
        return name?.getFileName() ?: super.toString()
    }

    override fun equals(other: Any?): Boolean {
        return other is Directory && other.id == id
    }

    fun String.getFileName(): String = File(this).name

}