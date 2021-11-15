package io.github.iamfacetheflames.rangpur.presenter

import io.github.iamfacetheflames.rangpur.data.*
import io.github.iamfacetheflames.rangpur.model.CachedDirectories
import io.github.iamfacetheflames.rangpur.model.Models
import io.github.iamfacetheflames.rangpur.model.PlaylistToFile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class LibraryPresenter(val scope: CoroutineScope, val models: Models, val router: Router) {

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
                val audios = models.audioLibrary.getAudios(filter)
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
                val file = router.openSaveFileDialog("Куда сохранить:", "playlist.m3u8", "m3u file","m3u8","m3u")
                withContext(Dispatchers.IO) {
                    if (file != null) {
                        exportAudioListInM3u(file.parentFile, file.name, false)
                    }
                }
            }
        }
    }

    fun openFilteredAudiosInExternalPlayer() {
        scope.launch(Dispatchers.IO) {
            exportAudioListInM3u(File(File(".").canonicalPath), "RangExternalPlaylist.m3u8", true)
        }
    }

    fun exportAudioListInM3u(directoryForM3u: File?, fileName: String, openM3uInExternalPlayer: Boolean) {
        if (directoryForM3u != null) {
            val audios = models.audioLibrary.getAudios(filter)
            val cachedDirectories = CachedDirectories(models.database, models.config)
            val file =
                PlaylistToFile.exportPlaylistM3u8(
                    fileName,
                    directoryForM3u.absolutePath,
                    audios,
                    cachedDirectories
                )
            cachedDirectories.release()
            if (openM3uInExternalPlayer) {
                router.openM3uOnExternalApp(file)
            }
        }
    }

    fun getAudioLocation(audio: Audio): File {
        val cachedDirs = CachedDirectories(models.database, models.config)
        val fileLocation = cachedDirs.getFullPath(audio.directoryUUID) + "/" + audio.fileName
        cachedDirs.release()
        return File(fileLocation)
    }

}