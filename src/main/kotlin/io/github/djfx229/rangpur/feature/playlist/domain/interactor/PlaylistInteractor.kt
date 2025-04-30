package io.github.djfx229.rangpur.feature.playlist.domain.interactor

import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.core.repository.database.Database
import io.github.djfx229.rangpur.feature.playlist.domain.model.AudioInPlaylist
import io.github.djfx229.rangpur.feature.playlist.domain.model.Playlist

class PlaylistInteractor(
    private val di: DependencyInjector,
) {

    private val database: Database by lazy { di.get() }

    fun getAudios(playlist: Playlist): List<AudioInPlaylist> {
        return database.playlistWithAudios.getFrom(playlist.uuid)
    }

}
