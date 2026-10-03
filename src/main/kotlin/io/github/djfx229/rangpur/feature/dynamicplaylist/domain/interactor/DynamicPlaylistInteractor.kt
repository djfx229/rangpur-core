package io.github.djfx229.rangpur.feature.dynamicplaylist.domain.interactor

import io.github.djfx229.rangpur.common.domain.database.Database
import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.feature.library.domain.model.filter.Filter
import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilterItem
import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilteredAudioField
import io.github.djfx229.rangpur.feature.library.domain.repository.LibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*


class DynamicPlaylistInteractor(
    private val di: DependencyInjector,
    private val database: Database,
) {
    private val libraryRepository: LibraryRepository by lazy { di.get() }
    private val coroutineScope: CoroutineScope by lazy { di.get() }

    private val audioListFlow = MutableStateFlow<List<Audio>>(mutableListOf())
    private var alreadyRequestedId: String = ""

    private var currentFilter: Filter = Filter(emptyList())
        set(value) {
            alreadyRequestedId = UUID.randomUUID().toString()
            field = value
        }

    fun observableAudioList(): StateFlow<List<Audio>> = audioListFlow

    fun generate() {
        database.directories.getOnlyRoot().randomOrNull()?.locationInMusicDirectory?.let { locationInMusicDirectory ->
            val item = FilterItem.TextSet(
                field = FilteredAudioField.DIRECTORY_LOCATION,
                values = setOf(locationInMusicDirectory),
                isNot = false,
            )
            currentFilter = Filter(listOf(item))
        }
    }

    fun generate(filter: Filter) {
        libraryRepository.clearAlreadyRequestedIds()
        currentFilter = filter
        requestNextAudios()
    }

    fun markListened(audio: Audio) {
        audioListFlow.value = audioListFlow.value.filter { it != audio }
        if (audioListFlow.value.size < 10) {
            requestNextAudios()
        }
    }

    private fun requestNextAudios() {
        coroutineScope.launch(Dispatchers.IO) {
            val audios = libraryRepository.getRandomAudios(alreadyRequestedId, currentFilter, 50)
            audioListFlow.value += audios
            if (audioListFlow.value.isEmpty()) {
                generate()
            }
        }
    }

}
