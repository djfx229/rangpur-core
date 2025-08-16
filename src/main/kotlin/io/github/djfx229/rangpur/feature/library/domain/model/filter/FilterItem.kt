package io.github.djfx229.rangpur.feature.library.domain.model.filter

import io.github.djfx229.rangpur.feature.library.domain.model.Keys

sealed class FilterItem(
    open val isNot: Boolean = false
) {
    data class Text(
        val field: FilteredAudioField,
        val value: String,
        override val isNot: Boolean,
    ) : FilterItem()

    data class Numeric(
        val field: FilteredAudioField,
        val value: Number,
        val max: Number? = null,
        override val isNot: Boolean,
    ) : FilterItem() {
        val min: Number = value
        val isRange: Boolean = max != null
    }

    data class KeyList(
        val keys: List<Keys.Key>,
        override val isNot: Boolean,
    ) : FilterItem()

    data class TextSet(
        val field: FilteredAudioField,
        val values: Set<String>,
        override val isNot: Boolean,
    ) : FilterItem()

    data class OnlyWithoutPlaylists(
        var isOnlyWithoutPlaylist: Boolean,
    ) : FilterItem()

    data class MultiplyItems(
        val items: List<FilterItem>,
    ) : FilterItem()

    data class Playlists(
        val uuidItems: List<String>,
        override val isNot: Boolean,
    ) : FilterItem()
}
