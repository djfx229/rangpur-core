package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.github.iamfacetheflames.rangpur.data.Audio
import io.github.iamfacetheflames.rangpur.data.AudioInPlaylist
import io.github.iamfacetheflames.rangpur.data.Directory

@DatabaseTable(tableName = "audio_in_playlist")
class OrmLiteAudioInPlaylist : AudioInPlaylist {

    @DatabaseField(columnName = "id", generatedId = true)
    override var id: Long = 0

    override var audio_id: Long = 0

    override var playlist_id: Long = 0

    @DatabaseField(columnName = "position")
    override var position: Int = 0

    @DatabaseField(columnName = "audio_id", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    var ormAudio: OrmLiteAudio? = null

    @DatabaseField(columnName = "playlist_id", foreign = true)
    var ormPlaylist: OrmLitePlaylist? = null

    override var audioObject: Audio?
        get() = ormAudio
        set(value) {
            if (value != null) {
                ormAudio = value as OrmLiteAudio
            }
        }

}