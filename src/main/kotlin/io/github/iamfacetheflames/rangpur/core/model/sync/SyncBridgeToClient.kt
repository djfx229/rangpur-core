package io.github.iamfacetheflames.rangpur.core.model.sync

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
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
        val isRequestedFile = fromClient.readObject() as Boolean
        return suspendCancellableCoroutine { continuation ->
            try {
                val file = File(filePath)
                if (isRequestedFile) {
                    FileInputStream(filePath).channel.use { stream ->
                        stream.transferTo(0, size, clientSocket)
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