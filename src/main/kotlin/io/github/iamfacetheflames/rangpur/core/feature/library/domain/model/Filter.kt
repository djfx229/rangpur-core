package io.github.iamfacetheflames.rangpur.core.feature.library.domain.model

import io.github.iamfacetheflames.rangpur.core.data.Directory

sealed class FilterItem {
    data class Text(
        val field: FilteredAudioField,
        val value: String,
    ) : FilterItem()

    data class Numeric(
        val field: FilteredAudioField,
        val value: Number,
    ) : FilterItem()

//    data class Objects(
//        val field: AudioField,
//        val objectsId: List<String>,
//    ) : FilterItem()

    data class Directories(
        val directories: List<Directory>
    ) : FilterItem()

    data class DateList(
        var dateList: List<String>,
    ) : FilterItem()
}

data class Filter(
    val items: List<FilterItem>,
)
