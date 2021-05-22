package io.github.iamfacetheflames.rangpur.data

interface PlaylistFolder {
    var id: Long
    var name: String?
    var timestampCreated: Long
    var parentId: Long
}