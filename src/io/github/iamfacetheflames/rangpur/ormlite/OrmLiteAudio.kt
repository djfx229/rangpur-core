package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.github.iamfacetheflames.rangpur.data.Audio
import io.github.iamfacetheflames.rangpur.data.AudioField.ALBUM
import io.github.iamfacetheflames.rangpur.data.AudioField.ALBUM_TRACK_NUMBER
import io.github.iamfacetheflames.rangpur.data.AudioField.ARTIST
import io.github.iamfacetheflames.rangpur.data.AudioField.BITRATE
import io.github.iamfacetheflames.rangpur.data.AudioField.BPM
import io.github.iamfacetheflames.rangpur.data.AudioField.COMMENT
import io.github.iamfacetheflames.rangpur.data.AudioField.DATE_CREATED
import io.github.iamfacetheflames.rangpur.data.AudioField.DIRECTORY_UUID
import io.github.iamfacetheflames.rangpur.data.AudioField.DURATION
import io.github.iamfacetheflames.rangpur.data.AudioField.ENCODER
import io.github.iamfacetheflames.rangpur.data.AudioField.FILE_NAME
import io.github.iamfacetheflames.rangpur.data.AudioField.ID
import io.github.iamfacetheflames.rangpur.data.AudioField.KEY
import io.github.iamfacetheflames.rangpur.data.AudioField.KEY_SORT_POSITION
import io.github.iamfacetheflames.rangpur.data.AudioField.SAMPLERATE
import io.github.iamfacetheflames.rangpur.data.AudioField.TIMESTAMP_CREATED
import io.github.iamfacetheflames.rangpur.data.AudioField.TITLE
import io.github.iamfacetheflames.rangpur.data.AudioField.URL
import io.github.iamfacetheflames.rangpur.data.Directory
import io.github.iamfacetheflames.rangpur.data.equalsUUID
import java.util.*

@DatabaseTable(tableName = "audio")
class OrmLiteAudio : Audio {

    @DatabaseField(columnName = "uuid", id = true, canBeNull = false, uniqueIndexName = "unique_uuid")
    override var uuid: String = UUID.randomUUID().toString()

    override var directory: Directory?
        get() = ormDirectory
        set(value) {ormDirectory = value as OrmLiteDirectory}

    @DatabaseField(columnName = FILE_NAME, canBeNull = false, uniqueIndexName = "unique_audio_item")
    override var fileName: String? = null

    @DatabaseField(columnName = ALBUM_TRACK_NUMBER, canBeNull = true)
    override var albumTrackNumber: Int? = null

    @DatabaseField(columnName = ARTIST, canBeNull = true)
    override var artist: String? = null

    @DatabaseField(columnName = TITLE, canBeNull = true)
    override var title: String? = null

    @DatabaseField(columnName = ALBUM, canBeNull = true)
    override var album: String? = null

    @DatabaseField(columnName = COMMENT, canBeNull = true)
    override var comment: String? = null

    @DatabaseField(columnName = URL, canBeNull = true)
    override var url: String? = null

    @DatabaseField(columnName = ENCODER, canBeNull = true)
    override var encoder: String? = null

    @DatabaseField(columnName = BITRATE, canBeNull = true)
    override var bitrate: Int? = null

    @DatabaseField(columnName = SAMPLERATE, canBeNull = true)
    override var samplerate: Int? = null

    @DatabaseField(columnName = KEY, canBeNull = true)
    override var key: Int? = null

    @DatabaseField(columnName = KEY_SORT_POSITION, canBeNull = false)
    override var keySortPosition: Int = 0

    @DatabaseField(columnName = BPM, canBeNull = true)
    override var bpm: Float? = null

    @DatabaseField(columnName = DURATION, canBeNull = false)
    override var duration: Long? = null

    @DatabaseField(columnName = DATE_CREATED, canBeNull = false)
    override var dateCreated: String? = null

    @DatabaseField(columnName = TIMESTAMP_CREATED, canBeNull = false)
    override var timestampCreated: Long = 0

    @DatabaseField(columnName = DIRECTORY_UUID, foreign = true, foreignAutoRefresh = true, uniqueIndexName = "unique_audio_item")
    var ormDirectory: OrmLiteDirectory? = null

    override fun toString(): String {
        return fileName ?: super.toString()
    }

    override fun equals(other: Any?): Boolean = equalsUUID(other)

}