package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.github.iamfacetheflames.rangpur.data.Playlist
import io.github.iamfacetheflames.rangpur.data.PlaylistFolder

@DatabaseTable(tableName = "playlist")
class OrmLitePlaylist : Playlist {

    @DatabaseField(columnName = "id", generatedId = true)
    override var id: Long = 0

    @DatabaseField(columnName = "name", canBeNull = false)
    override var name: String? = null

    @DatabaseField(columnName = "timestamp_created", canBeNull = false)
    override var timestampCreated: Long = 0

    override var folderId: Long = 0

    @DatabaseField(columnName = "folder_id", foreign = true, canBeNull = true)
    var ormFolder: OrmLitePlaylistFolder? = null

    override fun toString(): String {
        return name ?: super.toString()
    }

}