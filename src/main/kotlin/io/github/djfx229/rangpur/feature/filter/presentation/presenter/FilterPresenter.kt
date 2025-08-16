package io.github.djfx229.rangpur.feature.filter.presentation.presenter

import io.github.djfx229.rangpur.common.domain.di.DependencyInjector
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.Filter
import io.github.djfx229.rangpur.feature.filter.domain.model.filter.FilteredAudioField
import io.github.djfx229.rangpur.feature.filter.presentation.model.FilterFieldUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilterPresenter(
    private val di: DependencyInjector,
) {
    val fastSearchField = FilterFieldUi.FastSearch()
    val directoriesField = FilterFieldUi.Directories()
    val playlistsField = FilterFieldUi.Playlists()
    val datesField = FilterFieldUi.TextSet(FilteredAudioField.DATE_CREATED)

    private val flowFilterFieldsUi: MutableStateFlow<List<FilterFieldUi>> = MutableStateFlow(filterFieldUiList())
    private val flowFilter: MutableStateFlow<Filter> = MutableStateFlow(Filter(emptyList()))

    private val innerFields = listOf(
        fastSearchField,
        directoriesField,
        datesField,
        playlistsField,
    )

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
        playlistsField,
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
