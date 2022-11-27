package io.github.iamfacetheflames.rangpur.core.model.sync

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.IllegalStateException
import java.nio.channels.SocketChannel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SyncBridgeToServer(
    private val serverSocket: SocketChannel,
) : SyncBridge() {

    private val toServer: ObjectOutputStream = ObjectOutputStream(serverSocket.socket().getOutputStream())
    private val fromServer: ObjectInputStream = ObjectInputStream(serverSocket.socket().getInputStream())

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

    override suspend fun receiveFile(
        filePath: String
    ): Long {
        val size = fromServer.readObject<Long>()
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
                        fileFromServer.transferFrom(serverSocket, 0, size)
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

    override fun fromStream(): ObjectInputStream = fromServer

    override fun toStream(): ObjectOutputStream = toServer

    override val hostAddress: String = serverSocket.socket().inetAddress.hostAddress

}