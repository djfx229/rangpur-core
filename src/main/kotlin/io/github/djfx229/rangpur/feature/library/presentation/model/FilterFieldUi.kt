package io.github.djfx229.rangpur.feature.library.presentation.model

import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilterItem
import io.github.djfx229.rangpur.feature.library.domain.model.filter.FilteredAudioField
import io.github.djfx229.rangpur.feature.library.domain.model.Directory
import io.github.djfx229.rangpur.feature.library.domain.model.Keys
import io.github.djfx229.rangpur.feature.library.domain.model.Keys.plusAllCompatible
import io.github.djfx229.rangpur.feature.playlist.domain.model.Playlist

sealed class FilterFieldUi {
    abstract val name: String

    var isActive: Boolean = true

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
            item = FilterItem.Text(audioField, rawValue)
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
                        FilterItem.Numeric(audioField, min, max)
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                rawValue.toFloatOrNull()?.let { value ->
                    FilterItem.Numeric(audioField, value)
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
                values = selectedDirectories.mapNotNull { it.locationInMusicDirectory }.toSet()
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
                values = values
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
                            add(FilterItem.Text(it, rawValue))
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
                FilterItem.Playlists(selectedPlaylists.map { it.uuid })
            } else {
                null
            }
        }
    }

    protected fun parseKeyListFromString(rawValue: String): FilterItem.KeyList? {
        val rawValues = rawValue.split(" ")
        return if (rawValues.size == 2 && rawValues.last() == "+") {
            Keys.lancelotMap[rawValues.first().uppercase()]?.let { key ->
                FilterItem.KeyList(key.plusAllCompatible())
            }
        } else {
            val keys = rawValues.mapNotNull { rawValuesItem ->
                Keys.lancelotMap[rawValuesItem.uppercase()]
            }
            FilterItem.KeyList(keys)
        }
    }
}
