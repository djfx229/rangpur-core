package io.github.iamfacetheflames.rangpur.core.data

interface AudioInPlaylist : WithId {
    var audio: Audio?
    var playlist: Playlist?
    var position: Int
}