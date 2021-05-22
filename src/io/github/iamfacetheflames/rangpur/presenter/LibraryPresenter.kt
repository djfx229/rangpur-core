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

    var selectedAudios = mutableListOf<Audio>()
    private val filter = Filter()

    private val flowAudioList = MutableStateFlow<List<Audio>>(emptyList())

    fun observableFilteredAudios(): StateFlow<List<Audio>> = flowAudioList

    fun setFilterDate(date: String? = null) {
        scope.launch {
            if (date != null && date != DATE_ALL) {
                filter.dateList.clear()
                filter.dateList.add(date)
            } else {
                filter.dateList.clear()
            }
            requestAudioList()
        }
    }

    fun setSort(sort: Sort) {
        scope.launch {
            filter.sort = sort
            requestAudioList()
        }
    }

    fun setOnlyWithoutPlaylist(isOnlyWithoutPlaylist: Boolean) {
        scope.launch {
            filter.isOnlyWithoutPlaylist = isOnlyWithoutPlaylist
            requestAudioList()
        }
    }

    fun setFilterDate(dates: List<String>) {
        scope.launch {
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
        scope.launch {
            filter.searchRequest = searchText
            requestAudioList()
        }
    }

    fun requestAudioList() {
        scope.launch {
            val audios = models.audioLibrary.getAudios(filter)
            flowAudioList.value = audios
        }
    }

    fun exportFilteredAudiosInExternalPlaylist() {
        scope.launch {
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
        scope.launch {
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
        val fileLocation = cachedDirs.getFullPath(audio.directory!!) + "/" + audio.fileName
        cachedDirs.release()
        return File(fileLocation)
    }

}