package io.github.djfx229.rangpur.feature.sync

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

abstract class SyncBridge {

    abstract fun fromStream(): ObjectInputStream

    abstract fun toStream(): ObjectOutputStream

    abstract val hostAddress: String

    suspend fun <T> read(): T {
        return suspendCancellableCoroutine { continuation ->
            try {
                val value = fromStream().readObject()
                continuation.resume(value as T)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    suspend fun write(data: Any) {
        return suspendCancellableCoroutine { continuation ->
            try {
                toStream().writeObject(data)
                continuation.resume(Unit)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    open suspend fun sendFile(filePath: String): File {
        throw Exception("Метод не реализован")
    }

    open suspend fun receiveFile(filePath: String): Long {
        throw Exception("Метод не реализован")
    }

    suspend fun isStatusOk(): Boolean = read<String>() == Status.OK

    suspend fun done() = write(Command.DONE)

}