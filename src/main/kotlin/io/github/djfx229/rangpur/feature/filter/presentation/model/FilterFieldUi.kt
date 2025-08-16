package io.github.djfx229.rangpur.feature.filter.presentation.model

import io.github.djfx229.rangpur.feature.filter.domain.model.filter.FilterItem
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.FilteredAudioField
import io.github.djfx229.rangpur.feature.library.domain.model.Directory

sealed class FilterFieldUi(
    open val audioField: FilteredAudioField,
) {
    val name: String
        get() = audioField.name

    var isActive: Boolean = true

    var item: FilterItem? = null

    var rawValue: String = ""

    /**
     * Может выглядить избыточно, и вместо отдельных data class наследников можно было бы ввести type enum,
     * но в будущем для filter field будут кастомные поля, вместо единого rawValue.
     */

    class Text(
        override val audioField: FilteredAudioField,
    ) : FilterFieldUi(audioField)

    class Numeric(
        override val audioField: FilteredAudioField,
    ) : FilterFieldUi(audioField)

    class Key(
        override val audioField: FilteredAudioField,
    ) : FilterFieldUi(audioField)

    class Directories : FilterFieldUi(FilteredAudioField.DIRECTORY_LOCATION) {
        var selectedDirectories: Set<Directory> = emptySet()
    }

    class TextSet(
        override val audioField: FilteredAudioField,
    ) : FilterFieldUi(audioField) {
        var values: Set<String> = emptySet()
    }
}
