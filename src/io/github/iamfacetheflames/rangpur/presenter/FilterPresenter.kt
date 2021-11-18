package io.github.iamfacetheflames.rangpur.presenter

import io.github.iamfacetheflames.rangpur.data.*
import io.github.iamfacetheflames.rangpur.model.Models
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val DATE_ALL = "All"
const val DIRECTORY_ALL = "All"

class FilterPresenter(val scope: CoroutineScope, private val models: Models) {

    private val flowDateList = MutableStateFlow<List<String>>(emptyList())
    private val flowDirectories = MutableStateFlow<List<Directory>>(emptyList())
    private val flowPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    private var currentFolder: PlaylistFolder? = null

    fun observableDateList(): StateFlow<List<String>> = flowDateList
    fun observableDirectories(): StateFlow<List<Directory>> = flowDirectories
    fun observablePlaylists(): StateFlow<List<Playlist>> = flowPlaylists

    fun requestData(isDateOnlyYears: Boolean = false) {
        if (isDateOnlyYears) {
            requestFilterDateList()
        } else {
            requestFilterFullDateList()
        }
        requestFilterDirectories()
        requestPlaylists()
    }

    private fun requestFilterDateList(year: String? = null)  {
        val list = if (year == null) {
            models.filterLibrary.getYears()
        } else {
            models.filterLibrary.getMonths(year)
        }
        flowDateList.value = list
    }

    private fun requestFilterFullDateList()  {
        scope.launch(Dispatchers.IO) {
            val dates = mutableListOf<String>().apply {
                addAll(models.filterLibrary.getDateList())
            }
            flowDateList.value = dates
        }
    }

    private fun requestFilterDirectories(root: Directory? = null) {
        val list = if (root == null) {
            models.filterLibrary.getDirectories()
        } else {
            models.filterLibrary.getDirectories(root)
        }
        flowDirectories.value = list
    }

    private fun requestPlaylists(playlistFolder: PlaylistFolder? = null) {
        scope.launch(Dispatchers.IO) {
            val playlists = models.playlistLibrary.getPlaylists(playlistFolder)
            flowPlaylists.value = playlists
        }
    }

    val onFolderClicked: (PlaylistFolder) -> Unit = { folder ->
        if (folder is RootPlaylistFolder) {
            currentFolder = null
            requestPlaylists()
        } else {
            currentFolder = folder
            requestPlaylists(folder)
        }
    }

}