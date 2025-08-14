package io.github.djfx229.rangpur.feature.filter.presentation.model

import io.github.djfx229.rangpur.feature.filter.domain.model.filter.FilterItem
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.FilteredAudioField

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

    data class Text(
        override val audioField: FilteredAudioField,
    ) : FilterFieldUi(audioField)

    data class Numeric(
        override val audioField: FilteredAudioField,
    ) : FilterFieldUi(audioField)

    data class Key(
        override val audioField: FilteredAudioField,
    ) : FilterFieldUi(audioField)
}
