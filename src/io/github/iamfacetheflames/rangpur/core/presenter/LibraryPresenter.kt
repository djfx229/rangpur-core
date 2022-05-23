package io.github.iamfacetheflames.rangpur.core.presenter

import io.github.iamfacetheflames.rangpur.core.data.*
import io.github.iamfacetheflames.rangpur.core.model.AudioLibraryModel
import io.github.iamfacetheflames.rangpur.core.router.LibraryRouter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class LibraryPresenter(
    private val scope: CoroutineScope,
    private val audioLibraryModel: AudioLibraryModel,
    private val libraryRouter: LibraryRouter
) {

    private val filter = Filter()
    private val flowAudioList = MutableStateFlow<Result<List<Audio>>>(Result(emptyList<List<Audio>>()))
    private var requestAudioList: Job = Job()

    var selectedAudios = mutableListOf<Audio>()

    fun observableFilteredAudios(): StateFlow<Result<List<Audio>>> = flowAudioList

    fun setFilterDate(date: String? = null) {
        scope.launch(Dispatchers.IO) {
            if (date != null && date != DATE_ALL) {
                filter.dateList.clear()
                filter.dateList.add(date)
            } else {
                filter.dateList.clear()
            }
            requestAudioList()
        }
    }

    fun setMode(mode: Filter.Mode) {
        scope.launch(Dispatchers.IO) {
            filter.mode = mode
            requestAudioList()
        }
    }

    fun setPlaylist(playlistUUID: String) {
        scope.launch(Dispatchers.IO) {
            filter.playlistUUID = playlistUUID
            requestAudioList()
        }
    }

    fun setSort(sort: Sort) {
        scope.launch(Dispatchers.IO) {
            filter.sort = sort
            requestAudioList()
        }
    }

    fun setOnlyWithoutPlaylist(isOnlyWithoutPlaylist: Boolean) {
        scope.launch(Dispatchers.IO) {
            filter.isOnlyWithoutPlaylist = isOnlyWithoutPlaylist
            requestAudioList()
        }
    }

    fun setFilterDate(dates: List<String>) {
        scope.launch(Dispatchers.IO) {
            filter.dateList.clear()
            filter.dateList.addAll(dates)
            requestAudioList()
        }
    }

    fun setFilterDirectory(directory: Directory) {
        filter.directories.apply {
            clear()
            add(directory)
            requestAudioList()
        }
    }

    fun setFilterDirectories(directories: List<Directory>) {
        filter.directories.apply {
            clear()
            addAll(directories)
            requestAudioList()
        }
    }

    fun clearFilterDirectory() {
        filter.directories.apply {
            clear()
            requestAudioList()
        }
    }

    fun setFilterString(searchText: String) {
        scope.launch(Dispatchers.IO) {
            filter.searchRequest = searchText
            requestAudioList()
        }
    }

    fun requestAudioList() {
        if (requestAudioList.isActive) {
            requestAudioList.cancel()
            requestAudioList = Job()
        }
        scope.launch(Dispatchers.IO + requestAudioList) {
            try {
                flowAudioList.value = Result.waiting()
                val audios = audioLibraryModel.getAudios(filter)
                if (isActive) {
                    flowAudioList.value = Result(audios)
                }
            } catch (e: Exception) {
                flowAudioList.value = Result.failure(e)
            }
        }
    }

    fun exportFilteredAudiosInExternalPlaylist() {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val file = libraryRouter.openSaveFileDialog("Куда сохранить:", "playlist.m3u8", "m3u file","m3u8","m3u")
                withContext(Dispatchers.IO) {
                    if (file != null) {
                        exportAudioListInM3u(file.parentFile, file.name)
                    }
                }
            }
        }
    }

    fun openFilteredAudiosInExternalPlayer() {
        scope.launch(Dispatchers.IO) {
            val file = audioLibraryModel.createM3u8PlaylistWithFilteredAudios("RangExternalPlaylist.m3u8", filter)
            libraryRouter.openM3uOnExternalApp(file)
        }
    }

    fun exportAudioListInM3u(directoryForM3u: File?, fileName: String) {
        scope.launch(Dispatchers.IO) {
            if (directoryForM3u != null) {
                audioLibraryModel.createM3u8PlaylistWithFilteredAudios(directoryForM3u, fileName, filter)
            }
        }
    }

    fun openDirectoryContainsSelectedAudio() {
        scope.launch(Dispatchers.IO) {
            val item = selectedAudios.first()
            audioLibraryModel.getFullPath(item)?.let { file ->
                libraryRouter.openFileManager(file)
            }
        }
    }

    fun getSelectedAudios(
        selectedRows: IntArray,
        audios: List<AudioInPlaylist>
    ): Pair<
        MutableList<AudioInPlaylist>,
        MutableList<File>
    > {
        return audioLibraryModel.getSelectedAudios(selectedRows, audios)
    }

    val onPlaylistClicked: (Playlist) -> Unit = { playlist ->
        setPlaylist(playlist.uuid)
    }

}