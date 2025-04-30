package io.github.djfx229.rangpur.core.data

import io.github.djfx229.rangpur.feature.audio.domain.model.Audio

interface AudioInPlaylist : WithId {
    var audio: Audio?
    var playlist: Playlist?
    var position: Int
}