package io.github.iamfacetheflames.rangpur.data

interface AudioInPlaylist : WithId {
    var audio_id: Long
    var playlist_id: Long
    var audioObject: Audio?
    var position: Int
}