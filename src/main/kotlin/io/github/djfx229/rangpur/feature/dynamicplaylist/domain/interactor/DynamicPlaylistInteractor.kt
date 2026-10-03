package io.github.djfx229.rangpur.feature.dynamicplaylist.domain.interactor

import io.github.djfx229.rangpur.common.domain.Logger
import io.github.djfx229.rangpur.common.domain.database.Database
import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.feature.library.domain.model.filter.Filter
import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilterItem
import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilteredAudioField
import io.github.djfx229.rangpur.feature.library.domain.repository.LibraryRepository
import io.github.djfx229.rangpur.feature.player.domain.interactor.PlayerInteractor
import io.github.djfx229.rangpur.feature.player.domain.interactor.PlayerQueue
import io.github.djfx229.rangpur.feature.player.domain.interactor.SwitchDirection
import io.github.djfx229.rangpur.feature.player.domain.model.PlayerCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt


class DynamicPlaylistInteractor(
    private val di: DependencyInjector,
    private val database: Database,
) {
    private val log: Logger by lazy { di.get() }
    private val libraryRepository: LibraryRepository by lazy { di.get() }
    private val coroutineScope: CoroutineScope by lazy { di.get() }
    private val playerInteractor: PlayerInteractor by lazy { di.get() }

    private val audioListFlow = MutableStateFlow<List<Audio>>(mutableListOf())
    private val needUpdateCurrentTrackInAudioListFlow = MutableSharedFlow<Int?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private var alreadyRequestedId: String = ""

    private val someMagicCoefficient = 100
    private val minimumCount = (someMagicCoefficient * 0.5).roundToInt()
    private val nextPageSize = (someMagicCoefficient * 0.7).roundToInt()

    private var currentFilter: Filter = Filter(emptyList())
        set(value) {
            alreadyRequestedId = UUID.randomUUID().toString()
            field = value
        }

    fun buildDynamicQueue(
        currentAudio: Audio,
    ): PlayerQueue.DynamicQueue {
        return object : PlayerQueue.DynamicQueue() {
            init {
                currentItem = currentAudio
                needUpdateCurrentTrackInAudioListFlow.tryEmit(audioListFlow.value.indexOf(currentAudio))
            }

            override val isHasPrevious: Boolean
                get() = currentItem != audioListFlow.value.first()

            override val isHasNext: Boolean
                get() = currentItem != audioListFlow.value.last()

            override fun switchTo(
                where: SwitchDirection,
                isShuffleModeOn: Boolean,
                isInfinityModeOn: Boolean,
                isFullListened: Boolean,
                onSuccessfullySwitched: () -> Unit
            ) {
                val lastItem = currentItem ?: return
                val currentList = audioListFlow.value
                val newIndex = when (where) {
                    SwitchDirection.Previous -> {
                        if (!isHasPrevious) return
                        currentList.indexOf(lastItem) - 1
                    }
                    SwitchDirection.Next -> {
                        if (isHasNext) {
                            currentList.indexOf(lastItem) + 1
                        } else {
                            if (currentList.size > 1) {
                                0
                            } else {
                                return
                            }
                        }
                    }
                }
                currentList.getOrNull(newIndex)?.let { item ->
                    currentItem = item
                    trackFinished(lastItem, isFullListened)
                    needUpdateCurrentTrackInAudioListFlow.tryEmit(audioListFlow.value.indexOf(item))
                    onSuccessfullySwitched.invoke()
                }
            }

            override fun equals(other: Any?): Boolean {
                return audioListFlow.value == other
            }

            override fun hashCode(): Int {
                return audioListFlow.value.hashCode()
            }
        }
    }

    fun observableAudioList(): StateFlow<List<Audio>> = audioListFlow

    fun observableNeedUpdateAudioList(): SharedFlow<Int?> = needUpdateCurrentTrackInAudioListFlow

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

    fun generate(audio: Audio, filter: Filter) {
        coroutineScope.launch(Dispatchers.IO) {
            audioListFlow.value = listOf(audio)
            libraryRepository.clearAlreadyRequestedIds()
            currentFilter = filter
            requestNextAudios()
            playerInteractor.handleCommand(
                PlayerCommand.Open(
                    queue = buildDynamicQueue(audio),
                    doNotRestartPlaybackForSameTrack = true,
                )
            )
        }
    }

    /**
     * TODO: RNG-053 [isFullListened] потребуется для скоринга
     */
    fun trackFinished(audio: Audio, isFullListened: Boolean) {
        log.d(this, "trackFinished() ${audio.fileName} завершён, isFullListened=${isFullListened}")
        audioListFlow.value = audioListFlow.value.filter { it != audio }
        if (audioListFlow.value.size < minimumCount) {
            requestNextAudios()
        }
    }

    private fun requestNextAudios() {
        coroutineScope.launch(Dispatchers.IO) {
            val audios = libraryRepository.getRandomAudios(alreadyRequestedId, currentFilter, nextPageSize)
            audioListFlow.value += audios
            if (audioListFlow.value.isEmpty()) {
                generate()
            }
        }
    }

}
