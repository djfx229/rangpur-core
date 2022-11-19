package io.github.iamfacetheflames.rangpur.core.data

import io.github.iamfacetheflames.rangpur.core.model.CachedDirectories
import java.io.File

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

fun Audio.getFullPath(cachedDirs: CachedDirectories): String {
    val path = cachedDirs.getFullPath(
        this.directoryUUID
    )
    return path + universalSeparator + this.fileName
}