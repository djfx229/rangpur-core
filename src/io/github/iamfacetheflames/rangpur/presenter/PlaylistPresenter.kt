package io.github.iamfacetheflames.rangpur.presenter

import io.github.iamfacetheflames.rangpur.data.Audio
import io.github.iamfacetheflames.rangpur.data.AudioInPlaylist
import io.github.iamfacetheflames.rangpur.data.Playlist
import io.github.iamfacetheflames.rangpur.data.PlaylistFolder
import io.github.iamfacetheflames.rangpur.model.Models
import io.github.iamfacetheflames.rangpur.model.PlaylistToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PlaylistPresenter(val scope: CoroutineScope, val models: Models, val router: Router) {

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
        scope.launch {
            if (playlist != null) {
                val audios = models.audioLibrary.getAudios(playlist)
                flowAudioListFromPlaylist.value = audios
            }
        }
    }

    fun requestPlaylists(playlistFolder: PlaylistFolder? = null) {
        scope.launch {
            selectedAudios = mutableListOf<AudioInPlaylist>()
            val playlists = models.playlistLibrary.getPlaylists(playlistFolder)
            flowPlaylists.value = playlists
        }
    }

    fun requestPlaylistFolders() {
        scope.launch {
            val folders = models.playlistLibrary.getPlaylistFolders()
            flowPlaylistFolders.value = folders
        }
    }

    fun createPlaylistFolder(router: Router) {
        scope.launch {
            withContext(Dispatchers.Main) {
                val name = router.openInputDialog("Название будущей папки:")
                withContext(Dispatchers.IO) {
                    if (name != null) {
                        models.playlistLibrary.createPlaylistFolder(name)
                        requestPlaylistFolders()
                    }
                }
            }
        }
    }

    fun createPlaylist(router: Router) {
        scope.launch {
            withContext(Dispatchers.Main) {
                val name = router.openInputDialog("Название будущего плейлиста:")
                withContext(Dispatchers.IO) {
                    if (name != null) {
                        models.playlistLibrary.createPlaylist(name, currentFolder)
                        requestPlaylists(currentFolder)
                    }
                }
            }
        }
    }

    fun renamePlaylist(playlist: Playlist, router: Router) {
        scope.launch {
            withContext(Dispatchers.Main) {
                val value = playlist.name
                val name = router.openInputDialog("Новое имя плейлиста:", value)
                withContext(Dispatchers.IO) {
                    if (name != null) {
                        models.playlistLibrary.renamePlaylist(name, playlist)
                        requestPlaylists(currentFolder)
                    }
                }
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        scope.launch {
            withContext(Dispatchers.IO) {
                models.playlistLibrary.removePlaylist(playlist)
                requestPlaylists(currentFolder)
            }
        }
    }

    fun addAudiosInCurrentPlaylist(audios: List<Audio>) {
        val playlist = currentPlaylist
        if (playlist != null) {
            models.playlistLibrary.addAudiosInPlaylist(audios, playlist)
            flowAudioListFromPlaylist.value = models.audioLibrary.getAudios(playlist)
        }
    }

    fun deleteAudiosFromCurrentPlaylist(audios: List<AudioInPlaylist>) {
        val playlist = currentPlaylist
        if (playlist != null) {
            models.playlistLibrary.deleteAudiosFromPlaylist(audios, playlist)
            val newAudioList = LinkedList<AudioInPlaylist>()
            newAudioList.addAll(flowAudioListFromPlaylist.value)
            newAudioList.removeAll(audios)
            flowAudioListFromPlaylist.value = newAudioList
        }
    }

    fun moveAudiosInPlaylistToNewPosition(audios: List<AudioInPlaylist>, newPosition: Int) {
        scope.launch {
            val newAudioList = LinkedList<AudioInPlaylist>()
            val audioToNewPositionMap = mutableMapOf<AudioInPlaylist, Int>()
            audios.forEachIndexed { index, audioInPlaylist ->
                val audio = audioInPlaylist.audioObject ?: return@forEachIndexed
                println("audio: '${audio.fileName}' move from ${flowAudioListFromPlaylist.value.indexOf(audioInPlaylist)} to ${newPosition + index}")
                audioToNewPositionMap.put(audioInPlaylist, newPosition + index)
            }
            newAudioList.addAll(flowAudioListFromPlaylist.value)
            audioToNewPositionMap.forEach { audio: AudioInPlaylist, position: Int ->
                newAudioList.remove(audio)
                newAudioList.add(position, audio)
            }
            models.database.moveAudiosInPlaylistToNewPosition(newAudioList, newPosition)
            flowAudioListFromPlaylist.value = newAudioList
        }
    }

    fun exportPlaylistToTextFile(playlist: Playlist) {
        scope.launch {
            withContext(Dispatchers.Main) {
                val file = router.openSaveFileDialog("Куда сохранить:", "${playlist.name}.txt", "text file","txt") ?: return@withContext
                withContext(Dispatchers.IO) {
                    val audios = models.database.getPlaylistAudios(playlist)
                    PlaylistToFile.exportPlaylistTxt(file.name, file.parentFile.absolutePath, audios)
                }
            }
        }
    }

}