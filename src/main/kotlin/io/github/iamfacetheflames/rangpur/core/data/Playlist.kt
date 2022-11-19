package io.github.iamfacetheflames.rangpur.core.data

interface Playlist : WithId {
    var name: String?
    var timestampCreated: Long
    var folder: PlaylistFolder?
}