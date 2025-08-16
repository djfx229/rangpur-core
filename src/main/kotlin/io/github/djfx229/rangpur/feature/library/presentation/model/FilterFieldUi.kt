package io.github.djfx229.rangpur.feature.library.presentation.model

import io.github.djfx229.rangpur.feature.library.domain.model.Directory
import io.github.djfx229.rangpur.feature.library.domain.model.Keys
import io.github.djfx229.rangpur.feature.library.domain.model.Keys.plusAllCompatible
import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilterItem
import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilteredAudioField
import io.github.djfx229.rangpur.feature.playlist.domain.model.Playlist
import kotlin.math.max
import kotlin.math.min

sealed class FilterFieldUi {
    abstract val name: String

    var isActive: Boolean = true

    var isNot: Boolean = false

    var item: FilterItem? = null

    var rawValue: String = ""

    /**
     * Генерирует из введённых данных в [FilterFieldUi] данных необходимый [FilterItem]
     */
    abstract fun parse()

    /**
     * Может выглядить избыточно, и вместо отдельных data class наследников можно было бы ввести type enum,
     * но в будущем для filter field будут кастомные поля, вместо единого rawValue.
     */

    abstract class OneColumnField(
        open val audioField: FilteredAudioField,
    ) : FilterFieldUi() {
        override val name: String
            get() = audioField.name
    }

    class Text(
        override val audioField: FilteredAudioField,
    ) : OneColumnField(audioField) {
        override fun parse() {
            item = FilterItem.Text(
                field = audioField,
                value = rawValue,
                isNot = isNot,
            )
        }
    }

    class Numeric(
        override val audioField: FilteredAudioField,
    ) : OneColumnField(audioField) {
        override fun parse() {
            item = if (rawValue.contains("-")) {
                val rangeValues = rawValue.split("-")
                if (rangeValues.size == 2) {
                    val min = rangeValues.first().toFloatOrNull()
                    val max = rangeValues.last().toFloatOrNull()
                    if (min != null && max != null) {
                        FilterItem.Numeric(
                            field = audioField,
                            value = min,
                            max = max,
                            isNot = isNot,
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                rawValue.toFloatOrNull()?.let { value ->
                    FilterItem.Numeric(
                        field = audioField,
                        value = value,
                        isNot = isNot,
                    )
                }
            }
        }
    }

    class Key(
        override val audioField: FilteredAudioField,
    ) : OneColumnField(audioField) {
        override fun parse() {
            item = parseKeyListFromString(rawValue)
        }
    }

    class Directories : OneColumnField(FilteredAudioField.DIRECTORY_LOCATION) {
        var selectedDirectories: Set<Directory> = emptySet()
        override fun parse() {
            item = FilterItem.TextSet(
                field = FilteredAudioField.DIRECTORY_LOCATION,
                values = selectedDirectories.mapNotNull { it.locationInMusicDirectory }.toSet(),
                isNot = isNot,
            )
        }
    }

    class TextSet(
        override val audioField: FilteredAudioField,
    ) : OneColumnField(audioField) {
        var values: Set<String> = emptySet()
        override fun parse() {
            item = FilterItem.TextSet(
                field = audioField,
                values = values,
                isNot = isNot,
            )
        }
    }

    class FastSearch : FilterFieldUi() {
        override val name: String = "FAST_SEARCH"

        private val audioFieldsForSearch = listOf(
            FilteredAudioField.FILE_NAME,
            FilteredAudioField.DATE_CREATED,
            FilteredAudioField.ARTIST,
            FilteredAudioField.TITLE,
            FilteredAudioField.ALBUM,
            FilteredAudioField.COMMENT,
            FilteredAudioField.BITRATE,
            FilteredAudioField.BPM,
            FilteredAudioField.DIRECTORY_LOCATION,
        )

        override fun parse() {
            item = if (rawValue.isNotBlank()) {
                FilterItem.MultiplyItems(
                    items = buildList {
                        audioFieldsForSearch.forEach {
                            add(
                                FilterItem.Text(
                                    field = it,
                                    value = rawValue,
                                    isNot = isNot,
                                )
                            )
                        }
                        parseKeyListFromString(rawValue)?.let {
                            add(it)
                        }
                    }
                )
            } else {
                null
            }
        }
    }

    class Playlists : FilterFieldUi() {
        override val name: String = "PLAYLISTS"

        var isOnlyWithoutPlaylist: Boolean = false

        var selectedPlaylists: List<Playlist> = emptyList()

        override fun parse() {
            item = if (isOnlyWithoutPlaylist) {
                FilterItem.OnlyWithoutPlaylists(isOnlyWithoutPlaylist)
            } else if (selectedPlaylists.isNotEmpty()) {
                FilterItem.Playlists(
                    uuidItems = selectedPlaylists.map { it.uuid },
                    isNot = isNot,
                )
            } else {
                null
            }
        }
    }

    class Duration : OneColumnField(FilteredAudioField.DURATION) {
        override fun parse() {
            val rangeValues = rawValue.trim().split("-")
            item = if (rangeValues.size == 2) {
                val first = parseTime(rangeValues.first())
                val second = parseTime(rangeValues.last())
                FilterItem.Numeric(
                    field = audioField,
                    value = min(first, second),
                    max = max(first, second),
                    isNot = isNot,
                )
            } else if (rawValue.isNotBlank()) {
                val time = parseTime(rawValue)
                FilterItem.Numeric(
                    field = audioField,
                    value = time,
                    isNot = isNot,
                )
            } else {
                null
            }
        }

        private fun parseTime(inputString: String): Long {
            val values = inputString.trim().split(":").reversed()
            val seconds = values.getOrNull(0)?.toLong() ?: 0L
            val minutes = values.getOrNull(1)?.toLong() ?: 0L
            val hours = values.getOrNull(2)?.toLong() ?: 0L
            return seconds + (minutes * 60) + (hours * 60 * 60)
        }
    }

    protected fun parseKeyListFromString(rawValue: String): FilterItem.KeyList? {
        val rawValues = rawValue.split(" ")
        return if (rawValues.size == 2 && rawValues.last() == "+") {
            Keys.lancelotMap[rawValues.first().uppercase()]?.let { key ->
                FilterItem.KeyList(
                    keys = key.plusAllCompatible(),
                    isNot = isNot,
                )
            }
        } else {
            val keys = rawValues.mapNotNull { rawValuesItem ->
                Keys.lancelotMap[rawValuesItem.uppercase()]
            }.filterNot { it.sortPosition == 0 }
            if (keys.isNotEmpty()) {
                FilterItem.KeyList(
                    keys = keys,
                    isNot = isNot,
                )
            } else {
                null
            }
        }
    }
}
