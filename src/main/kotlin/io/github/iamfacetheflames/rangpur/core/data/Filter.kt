package io.github.iamfacetheflames.rangpur.core.data

import java.util.*

class Filter {

    var mode: Mode = Mode.LIBRARY
    var playlistUUID: String? = null
    var dateList: LinkedList<String> = LinkedList<String>()
    var searchRequest = ""
    var directories: LinkedList<Directory> = LinkedList<Directory>()
    var sort: Sort = DefaultSort()
    var isOnlyWithoutPlaylist: Boolean = false

    fun isDateFiltered(): Boolean = dateList.isNotEmpty()
    fun isDirectoriesFiltered(): Boolean = directories.isNotEmpty()
    fun isSearchRequest(): Boolean = searchRequest.isNotEmpty()
    fun clear() {
        dateList.clear()
        directories.clear()
        searchRequest = ""
    }

    enum class Mode(val uiName: String) {
        LIBRARY("Фильтры"),
        PLAYLIST("Плейлисты")
    }

}