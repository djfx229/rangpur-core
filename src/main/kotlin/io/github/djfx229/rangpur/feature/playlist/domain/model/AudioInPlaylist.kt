package io.github.djfx229.rangpur.feature.playlist.domain.model

import io.github.djfx229.rangpur.feature.database.domain.model.WithId
import io.github.djfx229.rangpur.feature.library.domain.model.Audio

interface AudioInPlaylist : WithId {
    var audio: Audio?
    var playlist: Playlist?
    var position: Int
}