package io.github.djfx229.rangpur.ormlite.data

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.github.djfx229.rangpur.core.data.Playlist
import io.github.djfx229.rangpur.core.data.PlaylistFolder
import io.github.djfx229.rangpur.core.data.equalsUUID
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
        set(value) {
            if (value != null) {
                ormFolder = value as OrmLitePlaylistFolder
            } else {
                ormFolder = null
            }
        }

    @DatabaseField(columnName = "folder_uuid", foreign = true, canBeNull = true)
    private var ormFolder: OrmLitePlaylistFolder? = null

    override fun toString(): String {
        return name ?: super.toString()
    }

    override fun equals(other: Any?): Boolean = equalsUUID(other)

}