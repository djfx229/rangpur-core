//package io.github.iamfacetheflames.rangpur.core.model.sync
//
//import io.github.iamfacetheflames.rangpur.core.data.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.io.ObjectInputStream
//import java.io.ObjectOutputStream
//import java.net.InetSocketAddress
//import java.nio.channels.SocketChannel
//
//class SyncClientModel(
//    private val handler: ClientHandler
//) {
//
//    suspend fun start(
//        host: String = "localhost",
//        port: Int = PORT,
//        listener: (ClientSyncInfo) -> Unit
//    ) = withContext(Dispatchers.IO) {
//        println("runClient() start $host : $port")
//        SocketChannel.open(
//                InetSocketAddress(
//                        host,
//                        port
//                )
//        ).use { server ->
//            println("runClient() get server")
//            val toServer: ObjectOutputStream = ObjectOutputStream(server.socket().getOutputStream())
//            toServer.writeObject(Command.START_SYNC)
//            val fromServer: ObjectInputStream = ObjectInputStream(server.socket().getInputStream()) // todo close ?
//            // https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/io/ObjectInputStream.html
//            var command = fromServer.readObject() as String
//            while (command != Command.DONE) {
//                println("server send command: $command")
//                when (command) {
//                    Command.REQUEST_DIRECTORIES ->  handler.requestDirectories(toServer)
//                    Command.SEND_DIRECTORIES -> handler.sendDirectories(fromServer)
//
//                    Command.REQUEST_PLAYLIST_FOLDERS ->  handler.requestPlaylistFolders(toServer)
//                    Command.SEND_PLAYLIST_FOLDERS -> handler.sendPlaylistFolders(fromServer)
//
//                    Command.REQUEST_PLAYLISTS ->  handler.requestPlaylists(toServer)
//                    Command.SEND_PLAYLISTS -> handler.sendPlaylists(fromServer)
//
//                    Command.REQUEST_PLAYLIST_AUDIOS ->  handler.requestPlaylistAudios(toServer)
//                    Command.SEND_PLAYLIST_AUDIOS -> handler.sendPlaylistAudios(fromServer)
//
//                    Command.REQUEST_AUDIOS ->  handler.requestAudios(toServer)
//                    Command.SEND_AUDIO -> handler.sendAudios(fromServer)
//                    Command.NEW_AUDIOS_AMOUNT -> handler.newAudiosAmount(fromServer)
//                }
//                command = fromServer.readObject() as String
//            }
//            cachedDirs.release()
//            server.finishConnect()
//        }
//    }
//
//    // todo stop function
//}
//
//interface ClientHandler {
//    suspend fun requestDirectories(toServer: ObjectOutputStream)
//    suspend fun sendDirectories(fromServer: ObjectInputStream)
//
//    suspend fun requestPlaylistFolders(toServer: ObjectOutputStream)
//    suspend fun sendPlaylistFolders(fromServer: ObjectInputStream)
//
//    suspend fun requestPlaylists(toServer: ObjectOutputStream)
//    suspend fun sendPlaylists(fromServer: ObjectInputStream)
//
//    suspend fun requestPlaylistAudios(toServer: ObjectOutputStream)
//    suspend fun sendPlaylistAudios(fromServer: ObjectInputStream)
//
//    suspend fun requestAudios(toServer: ObjectOutputStream)
//    suspend fun sendAudios(fromServer: ObjectInputStream)
//    suspend fun newAudiosAmount(fromServer: ObjectInputStream)
//}
//
////class ClientHandlerImpl(
////    private val database: Database,
////) : ClientHandler {
////
////    private var audiosAmount = 0
////
////    override suspend fun requestDirectories(toServer: ObjectOutputStream) {
////        val directories = database.directories.getAll()
////        val set = mutableSetOf<String>()
////        directories.forEach { set.add(it.uuid) }
////        toServer.writeObject(set)
////    }
////
////    override suspend fun sendDirectories(fromServer: ObjectInputStream) {
////        val newDirectories = fromServer.readObject<List<Directory>>().sortedBy { it.uuid }
////        database.directories.update(newDirectories)
////    }
////
////    override suspend fun requestPlaylistFolders(toServer: ObjectOutputStream) {
////        val data = database.playlistFolders.getAll()
////        val set = mutableSetOf<String>()
////        data.forEach { set.add(it.uuid) }
////        toServer.writeObject(set)
////    }
////
////    override suspend fun sendPlaylistFolders(fromServer: ObjectInputStream) {
////        val data = fromServer.readObject<List<PlaylistFolder>>().sortedBy { it.uuid }
////        database.playlistFolders.create(data)
////    }
////
////    override suspend fun requestPlaylists(toServer: ObjectOutputStream) {
////        val data = database.playlists.getAll()
////        val set = mutableSetOf<String>()
////        data.forEach { set.add(it.uuid) }
////        toServer.writeObject(set)
////    }
////
////    override suspend fun sendPlaylists(fromServer: ObjectInputStream) {
////        val data = fromServer.readObject<List<Playlist>>().sortedBy { it.uuid }
////        database.playlists.create(data)
////    }
////
////    override suspend fun requestPlaylistAudios(toServer: ObjectOutputStream) {
////        val data = database.playlistWithAudios.getAll()
////        val set = mutableSetOf<String>()
////        data.forEach { set.add(it.uuid) }
////        toServer.writeObject(set)
////    }
////
////    override suspend fun sendPlaylistAudios(fromServer: ObjectInputStream) {
////        val data = fromServer.readObject<List<AudioInPlaylist>>().sortedBy { it.uuid }
////        database.playlistWithAudios.create(data)
////    }
////
////    override suspend fun requestAudios(toServer: ObjectOutputStream) {
////        val data = database.audios.getAll()
////        val set = mutableSetOf<String>()
////        data.forEach { set.add(it.uuid) }
////        toServer.writeObject(set)
////    }
////
////    override suspend fun sendAudios(fromServer: ObjectInputStream) {
////        val audio = fromServer.readObject<Audio>()
////        println("server send audio object with id ${audio.uuid} : ${audio.fileName}")
////        listener.invoke(
////            ReceivingAudio(
////                audio,
////                audiosProgress++,
////                audiosAmount
////            )
////        )
////        val path = audio.getFullPath(cachedDirs)
////        try {
////            val size = server.transferFileTo(toServer, fromServer, path)
////            val file = File(path)
////            if (file.exists() && size == file.length()) {
////                database.audios.create(mutableListOf(audio))
////                println("server send file")
////                toServer.writeObject(Command.DONE)
////            } else {
////                toServer.writeObject(Command.ERROR)
////                break
////            }
////        } catch (e: java.lang.Exception) {
////            toServer.writeObject(Command.ERROR)
////            break
////        }
////    }
////
////    override suspend fun newAudiosAmount(fromServer: ObjectInputStream) {
////        audiosAmount = fromServer.readObject<Int>()
////    }
////
////    private suspend fun <T> ObjectInputStream.readObject(): T {
////        return suspendCancellableCoroutine { continuation ->
////            try {
////                val value = this.readObject()
////                continuation.resume(value as T)
////            } catch (e: Exception) {
////                continuation.resumeWithException(e)
////            }
////        }
////    }
////
////}