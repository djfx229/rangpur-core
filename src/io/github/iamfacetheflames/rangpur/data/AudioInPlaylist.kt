package io.github.iamfacetheflames.rangpur.data

interface AudioInPlaylist {
    var id: Long
    var audio_id: Long
    var playlist_id: Long
    var audioObject: Audio?
    var position: Int
}