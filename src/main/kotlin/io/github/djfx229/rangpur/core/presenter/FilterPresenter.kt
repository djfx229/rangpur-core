package io.github.djfx229.rangpur.core.presenter

import io.github.djfx229.rangpur.core.data.*
import io.github.djfx229.rangpur.core.model.FilterLibraryModel
import io.github.djfx229.rangpur.core.model.PlaylistLibraryModel
import io.github.djfx229.rangpur.feature.playlist.domain.model.Playlist
import io.github.djfx229.rangpur.feature.playlist.domain.model.PlaylistFolder
import io.github.djfx229.rangpur.feature.playlist.domain.model.RootPlaylistFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val DATE_ALL = "All"
const val DIRECTORY_ALL = "All"

class FilterPresenter(
    private val scope: CoroutineScope,
    private val filterLibraryModel: FilterLibraryModel,
    private val playlistLibraryModel: PlaylistLibraryModel
) {

    private val flowDateList = MutableStateFlow<List<String>>(emptyList())
    private val flowDirectories = MutableStateFlow<List<Directory>>(emptyList())
    private val flowPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    private var currentFolder: PlaylistFolder? = null

    fun observableDateList(): StateFlow<List<String>> = flowDateList
    fun observableDirectories(): StateFlow<List<Directory>> = flowDirectories
    fun observablePlaylists(): StateFlow<List<Playlist>> = flowPlaylists

    fun requestData(isDateOnlyYears: Boolean = false, onlyRootDirectories: Boolean = true) {
        if (isDateOnlyYears) {
            requestFilterDateList()
        } else {
            requestFilterFullDateList()
        }
        requestFilterDirectories(onlyRootDirectories)
        requestPlaylists()
    }

    private fun requestFilterDateList(year: String? = null)  {
        val list = if (year == null) {
            filterLibraryModel.getYears()
        } else {
            filterLibraryModel.getMonths(year)
        }
        flowDateList.value = list
    }

    private fun requestFilterFullDateList()  {
        scope.launch(Dispatchers.IO) {
            val dates = mutableListOf<String>().apply {
                addAll(filterLibraryModel.getDateList())
            }
            flowDateList.value = dates
        }
    }

    private fun requestFilterDirectories(onlyRootDirectories: Boolean) {
        val list = filterLibraryModel.getDirectories(onlyRootDirectories)
        flowDirectories.value = list
    }

    private fun requestPlaylists(playlistFolder: PlaylistFolder? = null) {
        scope.launch(Dispatchers.IO) {
            val playlists = playlistLibraryModel.getPlaylists(playlistFolder)
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