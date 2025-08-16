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

    val fastSearchField = FilterFieldUi.FastSearch()
    val directoriesField = FilterFieldUi.Directories()
    private val innerFields = listOf(
        fastSearchField,
        directoriesField,
    )

    val datesField = FilterFieldUi.TextSet(FilteredAudioField.DATE_CREATED)

    fun observableFilterFields(): StateFlow<List<FilterFieldUi>> = flowFilterFieldsUi.asStateFlow()

    fun observableFilter(): StateFlow<Filter> = flowFilter.asStateFlow()

    fun updateFilterField(filterFieldUi: FilterFieldUi) {
        filterFieldUi.parse()
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
        val items = buildList {
            val addIfActive = { field: FilterFieldUi ->
                if (field.isActive) {
                    field.item?.let { filterItem ->
                        add(filterItem)
                    }
                }
            }
            flowFilterFieldsUi.value.forEach { field ->
                addIfActive(field)
            }
            innerFields.forEach { field ->
                addIfActive(field)
            }
        }
        return Filter(items)
    }

}
