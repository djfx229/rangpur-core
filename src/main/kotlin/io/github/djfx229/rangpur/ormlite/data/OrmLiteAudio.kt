package io.github.djfx229.rangpur.ormlite.data

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import io.github.djfx229.rangpur.core.data.Audio
import io.github.djfx229.rangpur.core.data.AudioField.ALBUM
import io.github.djfx229.rangpur.core.data.AudioField.ALBUM_TRACK_NUMBER
import io.github.djfx229.rangpur.core.data.AudioField.ARTIST
import io.github.djfx229.rangpur.core.data.AudioField.BITRATE
import io.github.djfx229.rangpur.core.data.AudioField.BPM
import io.github.djfx229.rangpur.core.data.AudioField.COMMENT
import io.github.djfx229.rangpur.core.data.AudioField.DATE_CREATED
import io.github.djfx229.rangpur.core.data.AudioField.DIRECTORY_UUID
import io.github.djfx229.rangpur.core.data.AudioField.DURATION
import io.github.djfx229.rangpur.core.data.AudioField.ENCODER
import io.github.djfx229.rangpur.core.data.AudioField.FILE_NAME
import io.github.djfx229.rangpur.core.data.AudioField.KEY
import io.github.djfx229.rangpur.core.data.AudioField.KEY_SORT_POSITION
import io.github.djfx229.rangpur.core.data.AudioField.SAMPLERATE
import io.github.djfx229.rangpur.core.data.AudioField.TIMESTAMP_CREATED
import io.github.djfx229.rangpur.core.data.AudioField.TITLE
import io.github.djfx229.rangpur.core.data.AudioField.URL
import io.github.djfx229.rangpur.core.data.equalsUUID
import java.util.*

@DatabaseTable(tableName = "audio")
class OrmLiteAudio : Audio {

    @DatabaseField(columnName = "uuid", id = true, canBeNull = false, uniqueIndexName = "unique_uuid")
    override var uuid: String = UUID.randomUUID().toString()

    override var directoryUUID: String
        get() = ormDirectory?.uuid!!
        set(value) {
            if (ormDirectory != null) {
                ormDirectory?.uuid = value
            } else {
                ormDirectory = OrmLiteDirectory().apply { uuid = value }
            }
        }

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

    @DatabaseField(columnName = DIRECTORY_UUID, foreign = true, uniqueIndexName = "unique_audio_item")
    var ormDirectory: OrmLiteDirectory? = null

    override fun toString(): String {
        return fileName ?: super.toString()
    }

    override fun equals(other: Any?): Boolean = equalsUUID(other)

}