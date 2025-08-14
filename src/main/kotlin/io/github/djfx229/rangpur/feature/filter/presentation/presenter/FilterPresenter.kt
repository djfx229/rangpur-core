package io.github.djfx229.rangpur.feature.filter.presentation.presenter

import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.feature.library.domain.model.Keys
import io.github.djfx229.rangpur.feature.library.domain.model.Keys.plusAllCompatible
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.Filter
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.FilterItem
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.FilteredAudioField
import io.github.djfx229.rangpur.feature.filter.presentation.model.FilterFieldUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilterPresenter(
    private val di: DependencyInjector,
) {

    private val flowFilterFieldsUi: MutableStateFlow<List<FilterFieldUi>> = MutableStateFlow(filterFieldUiList())
    private val flowFilter: MutableStateFlow<Filter> = MutableStateFlow(Filter(emptyList()))

    fun observableFilterFields(): StateFlow<List<FilterFieldUi>> = flowFilterFieldsUi.asStateFlow()

    fun observableFilter(): StateFlow<Filter> = flowFilter.asStateFlow()

    fun updateFilterField(filterFieldUi: FilterFieldUi) {
        filterFieldUi.item = when (filterFieldUi) {
            is FilterFieldUi.Text -> FilterItem.Text(filterFieldUi.audioField, filterFieldUi.rawValue)
            is FilterFieldUi.Numeric -> {
                if (filterFieldUi.rawValue.contains("-")) {
                    val rangeValues = filterFieldUi.rawValue.split("-")
                    if (rangeValues.size == 2) {
                        val min = rangeValues.first().toFloatOrNull()
                        val max = rangeValues.last().toFloatOrNull()
                        if (min != null && max != null) {
                            FilterItem.Numeric(filterFieldUi.audioField, min, max)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } else {
                    filterFieldUi.rawValue.toFloatOrNull()?.let { value ->
                        FilterItem.Numeric(filterFieldUi.audioField, value)
                    }
                }
            }
            is FilterFieldUi.Key -> {
                val rawValues = filterFieldUi.rawValue.split(" ")
                if (rawValues.size == 2 && rawValues.last() == "+") {
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

        flowFilter.tryEmit(makeFilter())
    }

    private fun filterFieldUiList() = listOf(
        FilterFieldUi.Text(FilteredAudioField.DATE_CREATED),
        FilterFieldUi.Text(FilteredAudioField.ARTIST),
        FilterFieldUi.Text(FilteredAudioField.TITLE),
        FilterFieldUi.Text(FilteredAudioField.ALBUM),
        FilterFieldUi.Text(FilteredAudioField.COMMENT),
        FilterFieldUi.Text(FilteredAudioField.FILE_NAME),
        FilterFieldUi.Numeric(FilteredAudioField.BITRATE),
        FilterFieldUi.Key(FilteredAudioField.KEY),
        FilterFieldUi.Numeric(FilteredAudioField.BPM),
    )

    private fun makeFilter(): Filter {
        val items = mutableListOf<FilterItem>()
        flowFilterFieldsUi.value.forEach { field ->
            if (field.isActive) {
                field.item?.let { filterItem ->
                    items.add(filterItem)
                }
            }
        }
        return Filter(items)
    }

}
