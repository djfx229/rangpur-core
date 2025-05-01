package io.github.djfx229.rangpur.feature.sync

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.channels.SocketChannel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SyncBridgeToClient(
    private val clientSocket: SocketChannel,
    private val toClient: ObjectOutputStream,
    private val fromClient: ObjectInputStream,
) : SyncBridge() {

    override suspend fun sendFile(
        filePath: String
    ): File {
        val file = File(filePath)
        val size = file.length()
        toClient.writeObject(size)
        // https://stackoverflow.com/questions/7379469/filechannel-transferto-for-large-file-in-windows
        val blockSize = Math.min(4096, size)
        val isRequestedFile = fromClient.readObject() as Boolean
        return suspendCancellableCoroutine { continuation ->
            try {
                val file = File(filePath)
                if (isRequestedFile) {
                    FileInputStream(filePath).channel.use { channel ->
                        var position: Long = 0
                        fun transferTo(): Boolean {
                            println("SyncBridgeToClient:sendFile() запуск channel.transferTo для передачи новой порции данных")
                            val data = channel.transferTo(position, blockSize, clientSocket)
                            println("SyncBridgeToClient:sendFile() destinationChannel.transferFrom успешно выполнен data: $data")
                            return data > 0
                        }
                        while (transferTo()) {
                            position += blockSize
                            println("SyncBridgeToClient:sendFile() текущая позиция: $position")
                        }
                        println("SyncBridgeToClient:sendFile() работа с каналом завершена")
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

    override fun fromStream(): ObjectInputStream = fromClient

    override fun toStream(): ObjectOutputStream = toClient

    override val hostAddress: String = clientSocket.socket().inetAddress.hostAddress

}