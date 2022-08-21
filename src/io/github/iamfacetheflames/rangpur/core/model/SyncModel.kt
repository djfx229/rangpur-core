package io.github.iamfacetheflames.rangpur.core.model

import io.github.iamfacetheflames.rangpur.core.data.*
import io.github.iamfacetheflames.rangpur.core.repository.Configuration
import io.github.iamfacetheflames.rangpur.core.repository.database.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.*
import java.lang.IllegalStateException
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object Command {
    const val START_SYNC = "COMMAND_START_SYNC"
    const val REQUEST_DIRECTORIES = "COMMAND_REQUEST_DIRECTORIES"
    const val SEND_DIRECTORIES = "COMMAND_SEND_DIRECTORIES"
    const val REQUEST_AUDIOS = "COMMAND_REQUEST_AUDIOS"
    const val SEND_AUDIO = "COMMAND_SEND_AUDIO"
    const val REQUEST_PLAYLIST_FOLDERS = "COMMAND_REQUEST_PLAYLIST_FOLDERS"
    const val SEND_PLAYLIST_FOLDERS = "COMMAND_SEND_PLAYLIST_FOLDERS"
    const val REQUEST_PLAYLISTS = "COMMAND_REQUEST_PLAYLISTS"
    const val SEND_PLAYLISTS = "COMMAND_SEND_PLAYLISTS"
    const val REQUEST_PLAYLIST_AUDIOS = "COMMAND_REQUEST_PLAYLIST_AUDIOS"
    const val SEND_PLAYLIST_AUDIOS = "COMMAND_SEND_PLAYLIST_AUDIOS"
    const val NEW_AUDIOS_AMOUNT = "COMMAND_NEW_AUDIOS_AMOUNT"
    const val DONE = "COMMAND_DONE"
    const val ERROR = "COMMAND_ERROR"
}

class SyncModel(
    private val database: Database,
    private val config: Configuration
) {

    val PORT: Int = 54286
    private var currentSocket: ServerSocketChannel? = null

    suspend fun runServer(
        host: String = "localhost",
        port: Int = PORT
    ) = runServer(
        host = host,
        port = port,
        println = ::println,
        listener = {
            // ignore
        },
    )

    fun stopServer() {
        currentSocket?.close()
    }

    suspend fun runServer(
        println: (String) -> Unit,
        listener: (ClientSyncInfo) -> Unit,
        host: String = config.getSyncHost(),
        port: Int = config.getSyncPort(),
    ) = withContext(Dispatchers.IO) {
        try {
            println("runServer() start $host : $port")
            val socket = ServerSocketChannel.open()
            currentSocket = socket
            socket.bind(InetSocketAddress(host, port))
            println("server sync: server running on port ${socket.socket().localPort}")
            socket.accept().use { client ->
                println("server sync: client connected : ${client.socket().inetAddress.hostAddress}")
                val fromClient = ObjectInputStream(client.socket().getInputStream())
                println("client send command: ${fromClient.readObject() as String}")
                val toClient = ObjectOutputStream(client.socket().getOutputStream())
                toClient.writeObject(Command.REQUEST_DIRECTORIES)
                val serverDirectories = database.directories.getAll()
                val clientDirectories = fromClient.readObject() as Set<String>
                val newDirectories = serverDirectories.filter { it.uuid !in clientDirectories }
                toClient.writeObject(Command.SEND_DIRECTORIES)
                toClient.writeObject(newDirectories)
                val cachedDirs = CachedDirectories(database, config)
                toClient.writeObject(Command.REQUEST_AUDIOS)
                val serverAudios = database.audios.getAll().sortedBy { it.uuid }
                val clientDataIds = fromClient.readObject() as Set<String>
                val newAudios = serverAudios.filter { it.uuid !in clientDataIds }
                toClient.writeObject(Command.NEW_AUDIOS_AMOUNT)
                val audiosAmount = newAudios.size
                toClient.writeObject(audiosAmount)
                for ((index, audio) in newAudios.withIndex()) {
                    listener.invoke(
                        ReceivingAudio(
                            audio,
                            index,
                            audiosAmount
                        )
                    )
                    toClient.writeObject(Command.SEND_AUDIO)
                    toClient.writeObject(audio)
                    client.sendFile(fromClient, toClient, audio.getFullPath(cachedDirs))
                    if (fromClient.readObject() != Command.DONE) {
                        break
                    }
                }
                syncDataWithClient(
                    toClient,
                    fromClient,
                    Command.REQUEST_PLAYLIST_FOLDERS,
                    Command.SEND_PLAYLIST_FOLDERS,
                    database.playlistFolders.getAll()
                )
                syncDataWithClient(
                    toClient,
                    fromClient,
                    Command.REQUEST_PLAYLISTS,
                    Command.SEND_PLAYLISTS,
                    database.playlists.getAll()
                )
                syncDataWithClient(
                    toClient,
                    fromClient,
                    Command.REQUEST_PLAYLIST_AUDIOS,
                    Command.SEND_PLAYLIST_AUDIOS,
                    database.playlistWithAudios.getAll()
                )
                toClient.writeObject(Command.DONE)
                cachedDirs.release()
            }
        } catch (e: Exception) {
            println(e.stackTraceToString())
        } finally {
            currentSocket = null
        }
    }

    suspend fun runClient(
        host: String = "localhost",
        port: Int = PORT,
        listener: (ClientSyncInfo) -> Unit
    ) = withContext(Dispatchers.IO) {
        println("runClient() start $host : $port")
        SocketChannel.open(
            InetSocketAddress(
                host,
                port
            )
        ).use { server ->
            println("runClient() get server")
            val toServer = ObjectOutputStream(server.socket().getOutputStream())
            toServer.writeObject(Command.START_SYNC)
            val fromServer = ObjectInputStream(server.socket().getInputStream())
            var command = fromServer.readObject() as String
            val cachedDirs = CachedDirectories(database, config)
            var audiosAmount = 0
            var audiosProgress = 0
            while (command != Command.DONE) {
                println("server send command: $command")
                when (command) {
                    Command.REQUEST_DIRECTORIES -> {
                        val directories = database.directories.getAll()
                        val set = mutableSetOf<String>()
                        directories.forEach { set.add(it.uuid) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_DIRECTORIES -> {
                        val newDirectories = fromServer.readObject<List<Directory>>().sortedBy { it.uuid }
                        database.directories.update(newDirectories)
                    }
                    Command.REQUEST_PLAYLIST_FOLDERS -> {
                        val data = database.playlistFolders.getAll()
                        val set = mutableSetOf<String>()
                        data.forEach { set.add(it.uuid) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_PLAYLIST_FOLDERS -> {
                        val data = fromServer.readObject<List<PlaylistFolder>>().sortedBy { it.uuid }
                        database.playlistFolders.create(data)
                    }
                    Command.REQUEST_PLAYLISTS -> {
                        val data = database.playlists.getAll()
                        val set = mutableSetOf<String>()
                        data.forEach { set.add(it.uuid) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_PLAYLISTS -> {
                        val data = fromServer.readObject<List<Playlist>>().sortedBy { it.uuid }
                        database.playlists.create(data)
                    }
                    Command.REQUEST_PLAYLIST_AUDIOS -> {
                        val data = database.playlistWithAudios.getAll()
                        val set = mutableSetOf<String>()
                        data.forEach { set.add(it.uuid) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_PLAYLIST_AUDIOS -> {
                        val data = fromServer.readObject<List<AudioInPlaylist>>().sortedBy { it.uuid }
                        database.playlistWithAudios.create(data)
                    }
                    Command.REQUEST_AUDIOS -> {
                        val data = database.audios.getAll()
                        val set = mutableSetOf<String>()
                        data.forEach { set.add(it.uuid) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_AUDIO -> {
                        val audio = fromServer.readObject<Audio>()
                        println("server send audio object with id ${audio.uuid} : ${audio.fileName}")
                        listener.invoke(
                            ReceivingAudio(
                                audio,
                                audiosProgress++,
                                audiosAmount
                            )
                        )
                        val path = audio.getFullPath(cachedDirs)
                        try {
                            val size = server.transferFileTo(toServer, fromServer, path)
                            val file = File(path)
                            if (file.exists() && size == file.length()) {
                                database.audios.create(mutableListOf(audio))
                                println("server send file")
                                toServer.writeObject(Command.DONE)
                            } else {
                                toServer.writeObject(Command.ERROR)
                                break
                            }
                        } catch (e: java.lang.Exception) {
                            toServer.writeObject(Command.ERROR)
                            break
                        }
                    }
                    Command.NEW_AUDIOS_AMOUNT -> {
                        audiosAmount = fromServer.readObject<Int>()
                    }
                }
                command = fromServer.readObject() as String
            }
            cachedDirs.release()
            server.finishConnect()
        }
    }

    private fun syncDataWithClient(
        toClient: ObjectOutputStream,
        fromClient: ObjectInputStream,
        requestCommand: String,
        sendCommand: String,
        serverData: List<WithId>
    ) {
        toClient.writeObject(requestCommand)
        val clientDataIds = fromClient.readObject() as Set<String>
        val newData = serverData.filter { it.uuid !in clientDataIds }
        toClient.writeObject(sendCommand)
        toClient.writeObject(newData)
    }

    private suspend fun <T> ObjectInputStream.readObject(): T {
        return suspendCancellableCoroutine { continuation ->
            try {
                val value = this.readObject()
                continuation.resume(value as T)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    private suspend fun SocketChannel.sendFile(
        fromClient: ObjectInputStream,
        stream: ObjectOutputStream,
        filePath: String
    ): File {
        val file = File(filePath)
        val size = file.length()
        stream.writeObject(size)
        val isRequestedFile = fromClient.readObject() as Boolean
        return suspendCancellableCoroutine { continuation ->
            try {
                val file = File(filePath)
                if (isRequestedFile) {
                    FileInputStream(filePath).channel.use { stream ->
                        stream.transferTo(0, size, this)
                        continuation.resume(file)
                    }
                } else {
                    continuation.resume(file)
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    private suspend fun SocketChannel.transferFileTo(
        toServer: ObjectOutputStream,
        objectInputStream: ObjectInputStream,
        filePath: String
    ): Long {
        val size = objectInputStream.readObject<kotlin.Long>()
        return suspendCancellableCoroutine { continuation ->
            try {
                val file = File(filePath)
                if (size <= 0L) continuation.resumeWithException(
                    IllegalStateException("Невалидный размер файла: $size bites")
                )
                if (!file.exists() || size > file.length()) {
                    if (file.exists()) {
                        file.delete()
                    } else {
                        file.parentFile?.let { parent ->
                            if (!parent.exists()) {
                                parent.mkdirs()
                            }
                        }
                    }
                    toServer.writeObject(true)
                    FileOutputStream(filePath).channel.use { fileFromServer ->
                        fileFromServer.transferFrom(this, 0, size)
                        continuation.resume(size)
                    }
                } else {
                    toServer.writeObject(false)
                    continuation.resume(size)
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

}