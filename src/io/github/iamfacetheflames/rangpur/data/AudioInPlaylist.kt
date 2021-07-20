package io.github.iamfacetheflames.rangpur.data

interface AudioInPlaylist : WithId {
    var audio: Audio?
    var playlist: Playlist?
    var position: Int
}