package io.github.iamfacetheflames.rangpur.core.feature.library.domain.model

import io.github.iamfacetheflames.rangpur.core.data.AudioField
import io.github.iamfacetheflames.rangpur.core.data.SortDirection

enum class SortedAudioField {
    TIMESTAMP_CREATED,
    KEY_SORT_POSITION,
}

data class Sort(
    val field: SortedAudioField,
    val direction: SortDirection,
)

// пока я не избавлюсь от старого фильтра, приходится считаться со старой апишкой сортировки
fun io.github.iamfacetheflames.rangpur.core.data.Sort.mapToNewSort(): Sort {
    if (columnName == "default" || columnName == AudioField.TIMESTAMP_CREATED) {
        return Sort(SortedAudioField.TIMESTAMP_CREATED, direction)
    }

    if (columnName == AudioField.KEY_SORT_POSITION) {
        return Sort(SortedAudioField.KEY_SORT_POSITION, direction)
    }

    return Sort(SortedAudioField.TIMESTAMP_CREATED, SortDirection.DESC)
}
