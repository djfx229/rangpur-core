package io.github.djfx229.rangpur.feature.playlist.domain.model

import io.github.djfx229.rangpur.common.domain.model.WithId

interface PlaylistFolder : WithId {
    var name: String?
    var timestampCreated: Long
    var parent: PlaylistFolder?
}