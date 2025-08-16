package io.github.djfx229.rangpur.feature.library.domain.model.filter

import io.github.djfx229.rangpur.feature.library.domain.model.Keys

sealed class FilterItem {
    data class Text(
        val field: FilteredAudioField,
        val value: String,
    ) : FilterItem()

    data class Numeric(
        val field: FilteredAudioField,
        val value: Number,
        val max: Number? = null,
    ) : FilterItem() {
        val min: Number = value
        val isRange: Boolean = max != null
    }

    data class KeyList(
        val keys: List<Keys.Key>,
    ) : FilterItem()

    data class TextSet(
        val field: FilteredAudioField,
        val values: Set<String>,
    ) : FilterItem()

    data class OnlyWithoutPlaylists(
        var isOnlyWithoutPlaylist: Boolean,
    ) : FilterItem()

    data class MultiplyItems(
        val items: List<FilterItem>,
    ) : FilterItem()

    data class Playlists(
        val uuidItems: List<String>,
    ) : FilterItem()
}
