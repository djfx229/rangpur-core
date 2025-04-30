package io.github.djfx229.rangpur.core.presenter

import io.github.djfx229.rangpur.core.model.AudioLibraryModel
import io.github.djfx229.rangpur.core.model.PlaylistLibraryModel
import io.github.djfx229.rangpur.core.router.PlaylistRouter
import io.github.djfx229.rangpur.feature.audio.domain.model.Audio
import io.github.djfx229.rangpur.feature.playlist.domain.model.AudioInPlaylist
import io.github.djfx229.rangpur.feature.playlist.domain.model.Playlist
import io.github.djfx229.rangpur.feature.playlist.domain.model.PlaylistFolder
import io.github.djfx229.rangpur.feature.playlist.domain.model.RootPlaylistFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PlaylistPresenter(
    private val scope: CoroutineScope,
    private val audioLibraryModel: AudioLibraryModel,
    private val playlistLibraryModel: PlaylistLibraryModel,
    private val router: PlaylistRouter
) {

    var selectedAudios = mutableListOf<AudioInPlaylist>()
    var currentPlaylist: Playlist? = null
    var currentFolder: PlaylistFolder? = null

    private val flowPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    private val flowPlaylistFolders = MutableStateFlow<List<PlaylistFolder>>(emptyList())
    private val flowAudioListFromPlaylist = MutableStateFlow<List<AudioInPlaylist>>(emptyList())

    fun observableAudioListFromPlaylist(): StateFlow<List<AudioInPlaylist>> = flowAudioListFromPlaylist
    fun observablePlaylists(): StateFlow<List<Playlist>> = flowPlaylists
    fun observablePlaylistFolders(): StateFlow<List<PlaylistFolder>> = flowPlaylistFolders

    fun requestAudioListForPlaylist(playlist: Playlist?) {
        scope.launch(Dispatchers.IO) {
            if (playlist != null) {
                val audios = audioLibraryModel.getAudios(playlist)
                flowAudioListFromPlaylist.value = audios
            }
        }
    }

    fun requestPlaylists(playlistFolder: PlaylistFolder? = null) {
        scope.launch(Dispatchers.IO) {
            selectedAudios = mutableListOf<AudioInPlaylist>()
            val playlists = playlistLibraryModel.getPlaylists(playlistFolder)
            flowPlaylists.value = playlists
        }
    }

    fun requestPlaylistFolders() {
        scope.launch(Dispatchers.IO) {
            val folders = playlistLibraryModel.getPlaylistFolders()
            flowPlaylistFolders.value = folders
        }
    }

    fun createPlaylistFolder() {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val name = router.openInputDialog("Название будущей папки:")
                withContext(Dispatchers.IO) {
                    if (name != null) {
                        playlistLibraryModel.createPlaylistFolder(name)
                        requestPlaylistFolders()
                    }
                }
            }
        }
    }

    fun createPlaylist() {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val name = router.openInputDialog("Название будущего плейлиста:")
                withContext(Dispatchers.IO) {
                    try {
                        if (name != null) {
                            playlistLibraryModel.createPlaylist(name, currentFolder)
                            requestPlaylists(currentFolder)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        router.showErrorMessage(e.message.toString())
                    }
                }
            }
        }
    }

    fun renamePlaylist(playlist: Playlist) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                val value = playlist.name
                val name = router.openInputDialog("Новое имя плейлиста:", value)
                withContext(Dispatchers.IO) {
                    if (name != null) {
                        playlistLibraryModel.renamePlaylist(name, playlist)
                        requestPlaylists(currentFolder)
                    }
                }
            }
        }
    }

    fun deletePlaylistFolder(folder: PlaylistFolder) {
        scope.launch(Dispatchers.IO) {
            try {
                playlistLibraryModel.removePlaylistFolder(folder)
                requestPlaylistFolders()
            } catch (e: Exception) {
                e.printStackTrace()
                router.showErrorMessage(e.message.toString())
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        scope.launch(Dispatchers.IO) {
            try {
                playlistLibraryModel.removePlaylist(playlist)
                requestPlaylists(currentFolder)
            } catch (e: Exception) {
                e.printStackTrace()
                router.showErrorMessage(e.message.toString())
            }
        }
    }

    fun addAudiosInCurrentPlaylist(audios: List<Audio>) {
        val playlist = currentPlaylist
        if (playlist != null) {
            playlistLibraryModel.addAudiosInPlaylist(audios, playlist)
            flowAudioListFromPlaylist.value = audioLibraryModel.getAudios(playlist)
        }
    }

    fun deleteAudiosFromCurrentPlaylist(audios: List<AudioInPlaylist>) {
        val playlist = currentPlaylist
        if (playlist != null) {
            playlistLibraryModel.deleteAudiosFromPlaylist(audios, playlist)
            val newAudioList = LinkedList<AudioInPlaylist>()
            newAudioList.addAll(flowAudioListFromPlaylist.value)
            newAudioList.removeAll(audios)
            flowAudioListFromPlaylist.value = newAudioList
        }
    }

    fun moveAudiosInPlaylistToNewPosition(selectedList: List<AudioInPlaylist>, movePosition: Int) {
        scope.launch(Dispatchers.IO) {
            try {
                val fullList = flowAudioListFromPlaylist.value
                if (
                    selectedList.isEmpty() ||
                    selectedList.containsAll(fullList)
                ) {
                    return@launch
                }
                val finalList = playlistLibraryModel.moveAudiosInPlaylistToNewPosition(
                    fullList, selectedList, movePosition
                ).filterNotNull()
                flowAudioListFromPlaylist.value = finalList
            } catch (e: Exception) {
                e.printStackTrace()
                router.showErrorMessage(e.message.toString())
            }
        }
    }

    fun exportPlaylistToTextFile(playlist: Playlist) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = withContext(Dispatchers.Main) {
                    router.openSaveFileDialog("Куда сохранить:", "${playlist.name}.txt", "text file","txt")
                } ?: return@launch
                playlistLibraryModel.exportPlaylistToTextFile(file, playlist)
            } catch (e: Exception) {
                e.printStackTrace()
                router.showErrorMessage(e.message.toString())
            }
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

    val onPlaylistClicked: (Playlist) -> Unit = { playlist ->
        currentPlaylist = playlist
        requestAudioListForPlaylist(playlist)
    }

}