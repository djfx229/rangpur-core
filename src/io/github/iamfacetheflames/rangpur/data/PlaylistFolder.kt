package io.github.iamfacetheflames.rangpur.data

interface PlaylistFolder : WithId {
    var name: String?
    var timestampCreated: Long
    var parent: PlaylistFolder?
}