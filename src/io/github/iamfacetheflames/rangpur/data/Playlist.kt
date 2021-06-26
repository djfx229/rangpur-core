package io.github.iamfacetheflames.rangpur.data

interface Playlist : WithId {
    var name: String?
    var timestampCreated: Long
    var folderId: Long
}