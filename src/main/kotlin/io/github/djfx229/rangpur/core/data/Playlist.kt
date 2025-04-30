package io.github.djfx229.rangpur.core.data

interface Playlist : WithId {
    var name: String?
    var timestampCreated: Long
    var folder: PlaylistFolder?
}