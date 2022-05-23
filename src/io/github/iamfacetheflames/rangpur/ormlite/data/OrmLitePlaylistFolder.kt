package io.github.iamfacetheflames.rangpur.ormlite.data

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.github.iamfacetheflames.rangpur.core.data.PlaylistFolder
import io.github.iamfacetheflames.rangpur.core.data.equalsUUID
import java.util.*

@DatabaseTable(tableName = "playlist_folder")
class OrmLitePlaylistFolder : PlaylistFolder {

    @DatabaseField(columnName = "uuid", id = true, canBeNull = false, uniqueIndexName = "unique_uuid")
    override var uuid: String = UUID.randomUUID().toString()

    @DatabaseField(columnName = "name", canBeNull = false)
    override var name: String? = null

    @DatabaseField(columnName = "timestamp_created", canBeNull = false)
    override var timestampCreated: Long = 0

    override var parent: PlaylistFolder?
        get() = ormParent
        set(value) { ormParent = value as OrmLitePlaylistFolder
        }

    @DatabaseField(columnName = "parent_uuid", foreign = true, canBeNull = true)
    var ormParent: OrmLitePlaylistFolder? = null

    override fun toString(): String {
        return name ?: super.toString()
    }

    override fun equals(other: Any?): Boolean = equalsUUID(other)

}