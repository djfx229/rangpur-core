package io.github.djfx229.rangpur.core.data

import io.github.djfx229.rangpur.common.domain.model.sort.Sort
import io.github.djfx229.rangpur.common.domain.model.sort.SortDirection
import io.github.djfx229.rangpur.common.domain.model.sort.SortedAudioField
import io.github.djfx229.rangpur.feature.audio.domain.model.Directory
import java.util.*

class Filter {

    var mode: Mode = Mode.LIBRARY
    var playlistUUID: String? = null
    var dateList: LinkedList<String> = LinkedList<String>()
    var searchRequest = ""
    var directories: LinkedList<Directory> = LinkedList<Directory>()
    var sort: Sort = Sort(
        field = SortedAudioField.TIMESTAMP_CREATED,
        direction = SortDirection.DESC,
    )
    var isOnlyWithoutPlaylist: Boolean = false

    fun isDateFiltered(): Boolean = dateList.isNotEmpty()
    fun isDirectoriesFiltered(): Boolean = directories.isNotEmpty()
    fun isSearchRequest(): Boolean = searchRequest.isNotEmpty()

    enum class Mode(val uiName: String) {
        LIBRARY("Фильтры"),
        PLAYLIST("Плейлисты")
    }

}