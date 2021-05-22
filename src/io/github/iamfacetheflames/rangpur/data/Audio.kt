package io.github.iamfacetheflames.rangpur.data

interface Audio {
    var id: Long
    var directory: Directory?
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