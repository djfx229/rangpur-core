package io.github.djfx229.rangpur.feature.player.domain.interactor

import io.github.djfx229.rangpur.feature.library.domain.interactor.LibraryInteractor
import io.github.djfx229.rangpur.feature.library.domain.model.Audio
import io.github.djfx229.rangpur.feature.player.domain.model.PlayerSource
import io.github.djfx229.rangpur.feature.player.domain.model.state.MetadataState
import io.github.djfx229.rangpur.feature.playlist.domain.model.AudioInPlaylist
import io.github.djfx229.rangpur.feature.radio.domain.model.RadioStation
import java.io.File

enum class SwitchDirection {
    Previous,
    Next,
}

/**
 * Определяет очередь того, что будет воспроизводить [PlayerInteractor].
 */
sealed class PlayerQueue<T> {
    abstract val isSupportsShuffle: Boolean
    abstract val isSupportsSeek: Boolean
    abstract val isHasPrevious: Boolean
    abstract val isHasNext: Boolean

    var currentItem: T? = null
        protected set

    abstract val currentAudio: Audio?

    abstract val currentMetadata: MetadataState

    /**
     * @param where в какую позицию необходимо переместиться.
     * @param isShuffleModeOn если true, нужно запросить следующий случайный трек (если [isSupportsShuffle] == false, то
     * он будет проигнорирован).
     * @param isInfinityModeOn если false, то при попытке переключится в направлении где нет больше элементов
     * необходимо закольцевать список. Поддержка этого параметра для отдельно взятого [PlayerQueue] опциональна.
     * @param onSuccessfullySwitched коллбек который вызывается, когда произошло переключение.
     */
    abstract fun switchTo(
        where: SwitchDirection,
        isShuffleModeOn: Boolean,
        isInfinityModeOn: Boolean,
        onSuccessfullySwitched: () -> Unit,
    )

    abstract fun currentPlayerSource(libraryInteractor: LibraryInteractor): PlayerSource

    data object Empty : PlayerQueue<Unit>() {
        override val isSupportsShuffle: Boolean = false
        override val isSupportsSeek: Boolean = false
        override val isHasPrevious: Boolean = false
        override val isHasNext: Boolean = false
        override val currentAudio: Audio? = null
        override val currentMetadata: MetadataState = MetadataState.EmptyMetadataState

        override fun switchTo(
            where: SwitchDirection,
            isShuffleModeOn: Boolean,
            isInfinityModeOn: Boolean,
            onSuccessfullySwitched: () -> Unit
        ) {}

        override fun currentPlayerSource(libraryInteractor: LibraryInteractor): PlayerSource = PlayerSource.Unsupported
    }

    /**
     * Конечный список.
     */
    abstract class DefaultQueue<T>(
        index: Int,
        item: T,
        private val queue: List<T>,
        private val onChangeCurrentIndex: (index: Int, item: T) -> Unit,
    ) : PlayerQueue<T>() {
        private var currentIndex: Int = -1
            set(value) {
                field = value
                currentItem?.let { item ->
                    onChangeCurrentIndex(shuffleNewIndexIfNeed(value), item)
                }
            }

        private var randomQueue: List<Int>? = null
        private var mapToRealIndex: Map<Int, Int>? = null

        override val isSupportsShuffle: Boolean = true

        override val isSupportsSeek: Boolean
            get() = currentItem !is RadioStation

        override val isHasPrevious: Boolean
            get() = currentIndex - 1 >= 0

        override val isHasNext: Boolean
            get() = currentIndex + 1 < queue.size

        init {
            currentIndex = index
            currentItem = item
        }

        override val currentAudio: Audio?
            get() {
                val item = currentItem ?: return null
                return when (item) {
                    is Audio -> item
                    is AudioInPlaylist -> item.audio
                    else -> null
                }
            }

        override fun equals(other: Any?): Boolean {
            return queue == other
        }

        override fun hashCode(): Int {
            return queue.hashCode()
        }

        override fun switchTo(
            where: SwitchDirection,
            isShuffleModeOn: Boolean,
            isInfinityModeOn: Boolean,
            onSuccessfullySwitched: () -> Unit,
        ) {
            if (isShuffleModeOn && randomQueue == null) {
                generateShuffleData()
            } else if (!isShuffleModeOn && randomQueue != null) {
                releaseShuffleData()
            }

            val requestIndex = when (where) {
                SwitchDirection.Previous -> currentIndex - 1
                SwitchDirection.Next -> currentIndex + 1
            }
            val size = queue.size

            val index = if (requestIndex < 0) {
                if (isInfinityModeOn) {
                    size - 1
                } else {
                    return
                }
            } else if (requestIndex >= size) {
                if (isInfinityModeOn) {
                    0
                } else {
                    return
                }
            } else {
                requestIndex
            }

            val newIndex = if (isShuffleModeOn) {
                randomQueue.orEmpty().getOrNull(index) ?: -1
            } else {
                index
            }

            val newItem = queue.getOrNull(newIndex) ?: return
            currentItem = newItem
            currentIndex = index
            onSuccessfullySwitched.invoke()
        }

        private fun generateShuffleData() {
            randomQueue = buildList {
                add(queue.indexOf(currentItem))
                addAll(
                    queue.mapIndexed { index, item ->
                        if (item == currentItem) null else index
                    }.filterNotNull().shuffled()
                )
            }
            mapToRealIndex = buildMap {
                randomQueue?.forEachIndexed { index, item ->
                    put(item, index)
                }
            }
        }

        private fun getRealPlaylistIndex(index: Int): Int {
            return if (randomQueue != null) {
                if (mapToRealIndex == null || queue.size != mapToRealIndex?.size) {
                    generateShuffleData()
                }
                mapToRealIndex?.get(index) ?: -1
            } else {
                index
            }
        }

        private fun shuffleNewIndexIfNeed(newIndex: Int): Int {
            return if (randomQueue != null) {
                randomQueue.orEmpty().getOrNull(newIndex) ?: -1
            } else {
                newIndex
            }
        }

        private fun releaseShuffleData() {
            currentIndex = randomQueue.orEmpty().getOrNull(currentIndex) ?: 0
            randomQueue = null
            mapToRealIndex = null
        }
    }

    class LibraryQueue(
        index: Int,
        item: Audio,
        queue: List<Audio>,
        onChangeCurrentIndex: (index: Int, item: Audio) -> Unit,
    ) : DefaultQueue<Audio>(index, item, queue, onChangeCurrentIndex) {
        override val currentMetadata: MetadataState
            get() {
                currentItem?.let { item ->
                    return MetadataState.AudioItem(item)
                }
                return MetadataState.EmptyMetadataState
            }

        override fun currentPlayerSource(libraryInteractor: LibraryInteractor): PlayerSource {
            currentItem?.let { item ->
                return PlayerSource.File(libraryInteractor.getFullPath(item))
            }
            return PlayerSource.Unsupported
        }
    }

    class PlaylistQueue(
        index: Int,
        item: AudioInPlaylist,
        queue: List<AudioInPlaylist>,
        onChangeCurrentIndex: (index: Int, item: AudioInPlaylist) -> Unit,
    ) : DefaultQueue<AudioInPlaylist>(index, item, queue, onChangeCurrentIndex) {
        override val currentMetadata: MetadataState
            get() {
                currentItem?.audio?.let { item ->
                    return MetadataState.AudioItem(item)
                }
                return MetadataState.EmptyMetadataState
            }

        override fun currentPlayerSource(libraryInteractor: LibraryInteractor): PlayerSource {
            currentItem?.audio?.let { item ->
                return PlayerSource.File(libraryInteractor.getFullPath(item))
            }
            return PlayerSource.Unsupported
        }
    }

    class FilesQueue(
        index: Int,
        item: File,
        queue: List<File>,
        onChangeCurrentIndex: (index: Int, item: File) -> Unit,
    ) : DefaultQueue<File>(index, item, queue, onChangeCurrentIndex) {
        override val currentMetadata: MetadataState
            get() {
                currentItem?.let { item ->
                    return MetadataState.File(item.name)
                }
                return MetadataState.EmptyMetadataState
            }

        override fun currentPlayerSource(libraryInteractor: LibraryInteractor): PlayerSource {
            currentItem?.let { item ->
                return PlayerSource.File(item.absolutePath)
            }
            return PlayerSource.Unsupported
        }
    }

    class RadioQueue(
        index: Int,
        item: RadioStation,
        queue: List<RadioStation>,
        onChangeCurrentIndex: (index: Int, item: RadioStation) -> Unit,
    ) : DefaultQueue<RadioStation>(index, item, queue, onChangeCurrentIndex) {
        override val currentMetadata: MetadataState
            get() {
                currentItem?.let { item ->
                    return MetadataState.Stream(item)
                }
                return MetadataState.EmptyMetadataState
            }

        override fun currentPlayerSource(libraryInteractor: LibraryInteractor): PlayerSource {
            currentItem?.let { item ->
                return PlayerSource.Stream(item.streamUrl)
            }
            return PlayerSource.Unsupported
        }
    }
}
