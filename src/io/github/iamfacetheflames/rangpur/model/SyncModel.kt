package io.github.iamfacetheflames.rangpur.model

import io.github.iamfacetheflames.rangpur.data.*
import io.github.iamfacetheflames.rangpur.repository.Configuration
import io.github.iamfacetheflames.rangpur.repository.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.*
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

class SyncModel(val database: Database, val config: Configuration) {

    private val PORT: Int = 54286

    suspend fun runServer(
        host: String = "localhost",
        port: Int = PORT
    ) = withContext(Dispatchers.IO) {
        try {
            println("runServer() start $host : $port")
            val socket = ServerSocketChannel.open()
            socket.bind(InetSocketAddress(host, port))
            println("server sync: server running on port ${socket.socket().localPort}")
            socket.accept().use { client ->
                println("server sync: client connected : ${client.socket().inetAddress.hostAddress}")
                val fromClient = ObjectInputStream(client.socket().getInputStream())
                println("client send command: ${fromClient.readObject() as String}")
                val toClient = ObjectOutputStream(client.socket().getOutputStream())
                toClient.writeObject(Command.REQUEST_DIRECTORIES)
                val serverDirectories = database.getDirectories()
                val clientDirectories = fromClient.readObject() as Set<Long>
                val newDirectories = serverDirectories.filter { it.id !in clientDirectories }
                toClient.writeObject(Command.SEND_DIRECTORIES)
                toClient.writeObject(newDirectories)
                val cachedDirs = CachedDirectories(database, config)
                toClient.writeObject(Command.REQUEST_AUDIOS)
                val serverAudios = database.getAudios().sortedBy { it.id }
                val clientDataIds = fromClient.readObject() as Set<Long>
                val newAudios = serverAudios.filter { it.id !in clientDataIds }
                toClient.writeObject(Command.NEW_AUDIOS_AMOUNT)
                toClient.writeObject(newAudios.size)
                for ((index, audio) in newAudios.withIndex()) {
                    toClient.writeObject(Command.SEND_AUDIO)
                    toClient.writeObject(audio)
                    client.sendFile(toClient, audio.getFullPath(cachedDirs))
                    if (fromClient.readObject() != Command.DONE) {
                        break
                    }
                }
                syncDataWithClient(
                        toClient,
                        fromClient,
                        Command.REQUEST_PLAYLIST_FOLDERS,
                        Command.SEND_PLAYLIST_FOLDERS,
                        database.getPlaylistFolders()
                )
                syncDataWithClient(
                        toClient,
                        fromClient,
                        Command.REQUEST_PLAYLISTS,
                        Command.SEND_PLAYLISTS,
                        database.getPlaylists()
                )
                syncDataWithClient(
                        toClient,
                        fromClient,
                        Command.REQUEST_PLAYLIST_AUDIOS,
                        Command.SEND_PLAYLIST_AUDIOS,
                        database.getPlaylistAudios()
                )
                toClient.writeObject(Command.DONE)
                cachedDirs.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                        val directories = database.getDirectories()
                        val set = mutableSetOf<Long>()
                        directories.forEach { set.add(it.id) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_DIRECTORIES -> {
                        val newDirectories = fromServer.readObject<List<Directory>>().sortedBy { it.id }
                        database.saveDirectories(newDirectories)
                    }
                    Command.REQUEST_PLAYLIST_FOLDERS -> {
                        val data = database.getPlaylistFolders()
                        val set = mutableSetOf<Long>()
                        data.forEach { set.add(it.id) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_PLAYLIST_FOLDERS -> {
                        val data = fromServer.readObject<List<PlaylistFolder>>().sortedBy { it.id }
                        database.savePlaylistFolders(data)
                    }
                    Command.REQUEST_PLAYLISTS -> {
                        val data = database.getPlaylists()
                        val set = mutableSetOf<Long>()
                        data.forEach { set.add(it.id) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_PLAYLISTS -> {
                        val data = fromServer.readObject<List<Playlist>>().sortedBy { it.id }
                        database.savePlaylists(data)
                    }
                    Command.REQUEST_PLAYLIST_AUDIOS -> {
                        val data = database.getPlaylistAudios()
                        val set = mutableSetOf<Long>()
                        data.forEach { set.add(it.id) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_PLAYLIST_AUDIOS -> {
                        val data = fromServer.readObject<List<AudioInPlaylist>>().sortedBy { it.id }
                        database.savePlaylistAudios(data)
                    }
                    Command.REQUEST_AUDIOS -> {
                        val data = database.getAudios()
                        val set = mutableSetOf<Long>()
                        data.forEach { set.add(it.id) }
                        toServer.writeObject(set)
                    }
                    Command.SEND_AUDIO -> {
                        val audio = fromServer.readObject<Audio>()
                        println("server send audio object with id ${audio.id} : ${audio.fileName}")
                        listener.invoke(
                            ReceivingAudio(
                                audio,
                                audiosProgress++,
                                audiosAmount
                            )
                        )
                        val path = audio.getFullPath(cachedDirs)
                        server.transferFileTo(fromServer, path)
                        if (File(path).exists()) {
                            database.saveAudios(mutableListOf(audio))
                            println("server send file")
                            toServer.writeObject(Command.DONE)
                        } else {
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
        val clientDataIds = fromClient.readObject() as Set<Long>
        val newData = serverData.filter { it.id !in clientDataIds }
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

    private suspend fun SocketChannel.sendFile(stream: ObjectOutputStream, filePath: String): File {
        val file = File(filePath)
        val size = file.length()
        stream.writeObject(size)
        return suspendCancellableCoroutine { continuation ->
            try {
                FileInputStream(filePath).channel.use { stream ->
                    stream.transferTo(0, size, this)
                    continuation.resume(File(filePath))
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    private suspend fun SocketChannel.transferFileTo(objectInputStream: ObjectInputStream, filePath: String): File {
        val size = objectInputStream.readObject<kotlin.Long>()
        return suspendCancellableCoroutine { continuation ->
            try {
                File(filePath).parentFile?.let { parent ->
                    if (!parent.exists()) {
                        parent.mkdirs()
                    }
                }
                FileOutputStream(filePath).channel.use { fileFromServer ->
                    fileFromServer.transferFrom(this, 0, size)
                    continuation.resume(File(filePath))
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

}