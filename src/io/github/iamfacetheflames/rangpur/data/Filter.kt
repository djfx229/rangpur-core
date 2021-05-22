package io.github.iamfacetheflames.rangpur.data

import java.util.*

class Filter {
    var dateList: LinkedList<String> = LinkedList<String>()
    var searchRequest = ""
    var directories: LinkedList<Directory> = LinkedList<Directory>()
    var sort: Sort = DateSort()
    var isOnlyWithoutPlaylist: Boolean = false

    fun isDateFiltered(): Boolean = dateList.isNotEmpty()
    fun isDirectoriesFiltered(): Boolean = directories.isNotEmpty()
    fun isSearchRequest(): Boolean = searchRequest.isNotEmpty()
    fun clear() {
        dateList.clear()
        directories.clear()
        searchRequest = ""
    }
}