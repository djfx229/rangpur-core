package io.github.iamfacetheflames.rangpur.core.model.sync

import io.github.iamfacetheflames.rangpur.core.data.SyncInfoReceivingAudio
import io.github.iamfacetheflames.rangpur.core.data.SyncInfo
import io.github.iamfacetheflames.rangpur.core.data.SyncInfoFinished
import io.github.iamfacetheflames.rangpur.core.data.WithId
import io.github.iamfacetheflames.rangpur.core.model.CachedDirectories
import io.github.iamfacetheflames.rangpur.core.repository.database.Database

interface ServerHandler {
    suspend fun connected(client: SyncBridge, listener: (SyncInfo) -> Unit)
}

class ServerHandlerImpl(
    private val database: Database,
    private val cachedDirectoriesFactory: () -> CachedDirectories,
) : ServerHandler {

    lateinit var listener: (SyncInfo) -> Unit

    private suspend fun syncDataWithClient(
        client: SyncBridge,
        requestCommand: String,
        sendCommand: String,
        serverData: List<WithId>
    ) {
        client.write(requestCommand)
        val clientDataIds = client.read<Set<String>>()
        val newData = serverData.filter { it.uuid !in clientDataIds }
        client.write(sendCommand)
        client.write(newData)
    }

    private suspend fun greeting(client: SyncBridge): Boolean {
        println("server sync: client connected : ${client.hostAddress}")
        client.write(Command.GREETING)
        val clientVersion = client.read<Int>()
        return if (clientVersion != SYNC_VERSION) {
            client.write(Command.ERROR)
            false
        } else {
            true
        }
    }

    private suspend fun directories(client: SyncBridge) {
        client.write(Command.REQUEST_DIRECTORIES)
        val serverDirectories = database.directories.getAll()
        val clientDirectories = client.read<Set<String>>()
        val newDirectories = serverDirectories.filter { it.uuid !in clientDirectories }
        client.write(Command.SEND_DIRECTORIES)
        client.write(newDirectories)
    }

    private suspend fun audios(client: SyncBridge) {
        val cachedDirs = cachedDirectoriesFactory()
        try {
            client.write(Command.REQUEST_AUDIOS)
            val serverAudios = database.audios.getAll().sortedBy { it.uuid }
            val clientDataIds = client.read<Set<String>>()
            val newAudios = serverAudios.filter { it.uuid !in clientDataIds }
            client.write(Command.NEW_AUDIOS_AMOUNT)
            val audiosAmount = newAudios.size
            client.write(audiosAmount)
            for ((index, audio) in newAudios.withIndex()) {
                listener.invoke(
                    SyncInfoReceivingAudio(
                        audio,
                        index,
                        audiosAmount
                    )
                )
                client.write(Command.SEND_AUDIO)
                client.write(audio)
                val file = cachedDirs.getFullAudioPath(audio)
                println("ServerHandlerImpl: audios() запуск отправки файла на клиент $file")
                client.sendFile(file)
                if (client.isStatusOk()) {
                    println("ServerHandlerImpl: audios() отправка аудио OK")
                } else {
                    println("ServerHandlerImpl: audios() отправка аудио ERROR")
                    client.write(Command.ERROR)
                    break
                }
            }
        } finally {
            cachedDirs.release()
        }
    }

    private suspend fun playlistFolders(client: SyncBridge) {
        syncDataWithClient(
            client,
            Command.REQUEST_PLAYLIST_FOLDERS,
            Command.SEND_PLAYLIST_FOLDERS,
            database.playlistFolders.getAll()
        )
    }

    private suspend fun playlists(client: SyncBridge) {
        syncDataWithClient(
            client,
            Command.REQUEST_PLAYLISTS,
            Command.SEND_PLAYLISTS,
            database.playlists.getAll()
        )
    }

    private suspend fun playlistWithAudios(client: SyncBridge) {
        syncDataWithClient(
            client,
            Command.REQUEST_PLAYLIST_AUDIOS,
            Command.SEND_PLAYLIST_AUDIOS,
            database.playlistWithAudios.getAll()
        )
    }

    override suspend fun connected(client: SyncBridge, listener: (SyncInfo) -> Unit) {
        this.listener = listener
        val waitingStartCommand = client.read<String>()
        if (waitingStartCommand == Command.START_SYNC && greeting(client)) {
            directories(client)
            audios(client)
            // Синхронизация плейлистов отключена, поскольку на мобильном клиенте нет возможности их посмотреть.
            // Синхронизация плейлистов не покрыта unit-тестами, поэтому при раскомментировании нужно это иметь в виду.
//            playlistFolders(client)
//            playlists(client)
//            playlistWithAudios(client)
        }
        client.done()
        listener(SyncInfoFinished)
    }

}