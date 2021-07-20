package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.github.iamfacetheflames.rangpur.data.Playlist
import io.github.iamfacetheflames.rangpur.data.PlaylistFolder
import io.github.iamfacetheflames.rangpur.data.equalsUUID
import java.util.*

@DatabaseTable(tableName = "playlist")
class OrmLitePlaylist : Playlist {

    @DatabaseField(columnName = "uuid", id = true, canBeNull = false, uniqueIndexName = "unique_uuid")
    override var uuid: String = UUID.randomUUID().toString()

    @DatabaseField(columnName = "name", canBeNull = false)
    override var name: String? = null

    @DatabaseField(columnName = "timestamp_created", canBeNull = false)
    override var timestampCreated: Long = 0

    override var folder: PlaylistFolder?
        get() = ormFolder
        set(value) { ormFolder = value as OrmLitePlaylistFolder }

    @DatabaseField(columnName = "folder_uuid", foreign = true, canBeNull = true)
    private var ormFolder: OrmLitePlaylistFolder? = null

    override fun toString(): String {
        return name ?: super.toString()
    }

    override fun equals(other: Any?): Boolean = equalsUUID(other)

}