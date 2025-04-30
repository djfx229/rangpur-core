package io.github.djfx229.rangpur.ormlite.data

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.github.djfx229.rangpur.core.data.*
import java.util.*

@DatabaseTable(tableName = "audio_in_playlist")
class OrmLiteAudioInPlaylist : AudioInPlaylist {

    @DatabaseField(columnName = "uuid", id = true, canBeNull = false, uniqueIndexName = "unique_uuid")
    override var uuid: String = UUID.randomUUID().toString()

    @DatabaseField(columnName = "position")
    override var position: Int = 0

    @DatabaseField(columnName = "audio_uuid", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    var ormAudio: OrmLiteAudio? = null

    @DatabaseField(columnName = "playlist_uuid", foreign = true)
    var ormPlaylist: OrmLitePlaylist? = null

    override var audio: Audio?
        get() = ormAudio
        set(value) {
            if (value != null) {
                ormAudio = value as OrmLiteAudio
            }
        }

    override var playlist: Playlist?
        get() = ormPlaylist
        set(value) {
            if (value != null) {
                ormPlaylist = value as OrmLitePlaylist
            }
        }

    override fun equals(other: Any?): Boolean = equalsUUID(other)

}