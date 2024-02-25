package io.github.iamfacetheflames.rangpur.core.model.sync

import io.github.iamfacetheflames.rangpur.core.data.*
import io.github.iamfacetheflames.rangpur.core.model.CachedDirectories
import io.github.iamfacetheflames.rangpur.core.repository.database.Database
import java.io.File

interface ClientHandler {
    suspend fun receiveCommand(command: String, server: SyncBridge)
    suspend fun connected(server: SyncBridge, listener: (SyncInfo) -> Unit)
}

class ClientHandlerImpl(
    private val database: Database,
    private val cachedDirectoriesFactory: () -> CachedDirectories,
) : ClientHandler {

    private var audiosProgress: Int = 0
    private var audiosAmount: Int = 0
    lateinit var listener: (SyncInfo) -> Unit

    private suspend fun greeting(server: SyncBridge) {
        server.write(SYNC_VERSION)
    }

    suspend fun requestDirectories(server: SyncBridge) {
        val directories = database.directories.getAll()
        val set = mutableSetOf<String>()
        directories.forEach { set.add(it.uuid) }
        server.write(set)
    }

    suspend fun sendDirectories(server: SyncBridge) {
        val newDirectories = server.read<List<Directory>>().sortedBy { it.uuid }
        database.directories.update(newDirectories)
    }

    suspend fun requestPlaylistFolders(server: SyncBridge) {
        val data = database.playlistFolders.getAll()
        val set = mutableSetOf<String>()
        data.forEach { set.add(it.uuid) }
        server.write(set)
    }

    suspend fun sendPlaylistFolders(server: SyncBridge) {
        val data = server.read<List<PlaylistFolder>>().sortedBy { it.uuid }
        database.playlistFolders.create(data)
    }

    suspend fun requestPlaylists(server: SyncBridge) {
        val data = database.playlists.getAll()
        val set = mutableSetOf<String>()
        data.forEach { set.add(it.uuid) }
        server.write(set)
    }

    suspend fun sendPlaylists(server: SyncBridge) {
        val data = server.read<List<Playlist>>().sortedBy { it.uuid }
        database.playlists.create(data)
    }

    suspend fun requestPlaylistAudios(server: SyncBridge) {
        val data = database.playlistWithAudios.getAll()
        val set = mutableSetOf<String>()
        data.forEach { set.add(it.uuid) }
        server.write(set)
    }

    suspend fun sendPlaylistAudios(server: SyncBridge) {
        val data = server.read<List<AudioInPlaylist>>().sortedBy { it.uuid }
        database.playlistWithAudios.createOrUpdate(data)
    }

    suspend fun requestAudios(server: SyncBridge) {
        val data = database.audios.getAll()
        val set = mutableSetOf<String>()
        data.forEach { set.add(it.uuid) }
        server.write(set)
    }

    suspend fun sendAudios(server: SyncBridge, audiosAmount: Int) {
        val cachedDirs = cachedDirectoriesFactory()
        val audio = server.read<Audio>()
        println("server send audio object with id ${audio.uuid} : ${audio.fileName}")
        listener.invoke(
            SyncInfoReceivingAudio(
                audio,
                audiosProgress++,
                audiosAmount
            )
        )
        val path = cachedDirs.getFullAudioPath(audio)
        try {
            val size = server.receiveFile(path)
            val file = File(path)
            if (file.exists() && size == file.length()) {
                database.audios.create(mutableListOf(audio))
                println("ClientHandlerImpl: sendAudios() успешно принят файл $file")
                server.write(Status.OK)
            } else {
                println("ClientHandlerImpl: sendAudios() файл не получилось принять или его размер не соответствует исходному $file")
                server.write(Command.ERROR)
                return
            }
        } catch (e: java.lang.Exception) {
            server.write(Command.ERROR)
            return
        } finally {
            cachedDirs.release()
        }
    }

    suspend fun newAudiosAmount(server: SyncBridge, audiosAmount: (Int) -> Unit) {
        audiosAmount(server.read<Int>())
    }

    override suspend fun connected(
        server: SyncBridge,
        listener: (SyncInfo) -> Unit,
    ) {
        this.listener = listener
        server.write(Command.START_SYNC)
        var command: String
        do {
            command = server.read<String>()
            println("server send command: $command")
            receiveCommand(command, server)
        } while (command != Command.DONE)
        listener(SyncInfoFinished)
    }

    override suspend fun receiveCommand(command: String, server: SyncBridge) {
        println("ClientHandlerImpl: принята команда от сервера: $command")
        when (command) {
            Command.GREETING -> greeting(server)
            Command.REQUEST_DIRECTORIES ->  requestDirectories(server)
            Command.SEND_DIRECTORIES -> sendDirectories(server)

            Command.REQUEST_PLAYLIST_FOLDERS ->  requestPlaylistFolders(server)
            Command.SEND_PLAYLIST_FOLDERS -> sendPlaylistFolders(server)

            Command.REQUEST_PLAYLISTS ->  requestPlaylists(server)
            Command.SEND_PLAYLISTS -> sendPlaylists(server)

            Command.REQUEST_PLAYLIST_AUDIOS ->  requestPlaylistAudios(server)
            Command.SEND_PLAYLIST_AUDIOS -> sendPlaylistAudios(server)

            Command.REQUEST_AUDIOS ->  requestAudios(server)
            Command.SEND_AUDIO -> sendAudios(server, audiosAmount)
            Command.NEW_AUDIOS_AMOUNT -> newAudiosAmount(server) {amount ->
                audiosProgress = 0
                audiosAmount = amount
            }
            Command.DONE -> return
            else -> throw Exception("Неизвестная команда")
        }
    }
    
}