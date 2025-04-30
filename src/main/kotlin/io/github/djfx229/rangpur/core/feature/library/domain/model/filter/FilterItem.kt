package io.github.djfx229.rangpur.core.feature.library.domain.model.filter

import io.github.iamfacetheflames.rangpur.core.data.Directory
import io.github.iamfacetheflames.rangpur.core.data.Keys

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

    data class Directories(
        val directories: List<Directory>
    ) : FilterItem()

    data class DateList(
        var dateList: List<String>,
    ) : FilterItem()

    data class OnlyWithoutPlaylists(
        var isOnlyWithoutPlaylist: Boolean,
    ) : FilterItem()
}