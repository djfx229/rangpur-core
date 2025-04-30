package io.github.djfx229.rangpur.core.data

import io.github.iamfacetheflames.rangpur.core.repository.database.Database
import java.sql.Date

interface Audio : WithId {
    var directoryUUID: String
    var fileName: String?
    var albumTrackNumber: Int?
    var artist: String?
    var title: String?
    var album: String?
    var comment: String?
    var url: String?
    var encoder: String?
    var bitrate: Int?
    var samplerate: Int?
    var key: Int?
    var keySortPosition: Int
    var bpm: Float?
    var duration: Long?
    var dateCreated: String?
    var timestampCreated: Long
}

fun Audio.copy(builder: Database.Builder): Audio {
    val copiedAudio = builder.createEmptyAudio()
    copyTo(copiedAudio)
    return copiedAudio
}

fun Audio.copyTo(saveToAudio: Audio) {
    saveToAudio.uuid = this.uuid
    saveToAudio.directoryUUID = this.directoryUUID
    saveToAudio.fileName = this.fileName
    saveToAudio.albumTrackNumber = this.albumTrackNumber
    saveToAudio.artist = this.artist
    saveToAudio.title = this.title
    saveToAudio.album = this.album
    saveToAudio.comment = this.comment
    saveToAudio.url = this.url
    saveToAudio.encoder = this.encoder
    saveToAudio.bitrate = this.bitrate
    saveToAudio.samplerate = this.samplerate
    saveToAudio.key = this.key
    saveToAudio.keySortPosition = this.keySortPosition
    saveToAudio.bpm = this.bpm
    saveToAudio.duration = this.duration
    saveToAudio.dateCreated = this.dateCreated
    saveToAudio.timestampCreated = this.timestampCreated
}

fun Audio.equalsAllFields(other: Any?): Boolean {
    return if (other is Audio) {
        other.uuid == this.uuid &&
        other.directoryUUID == this.directoryUUID &&
        other.fileName == this.fileName &&
        other.albumTrackNumber == this.albumTrackNumber &&
        other.artist == this.artist &&
        other.title == this.title &&
        other.album == this.album &&
        other.comment == this.comment &&
        other.url == this.url &&
        other.encoder == this.encoder &&
        other.bitrate == this.bitrate &&
        other.samplerate == this.samplerate &&
        other.key == this.key &&
        other.keySortPosition == this.keySortPosition &&
        other.bpm == this.bpm &&
        other.duration == this.duration &&
        other.dateCreated == this.dateCreated &&
        other.timestampCreated == this.timestampCreated
    } else {
        false
    }
}

class TestAudio(
    override var fileName: String?,
    override var artist: String?,
    override var title: String?,
    override var directoryUUID: String = "",
    override var albumTrackNumber: Int? = 0,
    override var album: String? = "",
    override var comment: String? = "",
    override var url: String? = "",
    override var encoder: String? = "",
    override var bitrate: Int? = null,
    override var samplerate: Int? = null,
    override var key: Int? = null,
    override var keySortPosition: Int = 0,
    override var bpm: Float? = 128f,
    override var duration: Long? = 10L,
    override var dateCreated: String? = "2016-09-01",
    override var timestampCreated: Long = Date.valueOf("2016-09-01").time,
    override var uuid: String = generateUuid(),
) : TestItem(), Audio