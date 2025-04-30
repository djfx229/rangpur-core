package io.github.djfx229.rangpur.core.data

interface PlaylistFolder : WithId {
    var name: String?
    var timestampCreated: Long
    var parent: PlaylistFolder?
}