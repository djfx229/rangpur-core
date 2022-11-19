package io.github.iamfacetheflames.rangpur.core.data

interface PlaylistFolder : WithId {
    var name: String?
    var timestampCreated: Long
    var parent: PlaylistFolder?
}