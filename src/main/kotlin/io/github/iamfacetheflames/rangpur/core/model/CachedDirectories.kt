package io.github.iamfacetheflames.rangpur.core.model

import io.github.iamfacetheflames.rangpur.core.data.Audio
import io.github.iamfacetheflames.rangpur.core.data.Directory
import io.github.iamfacetheflames.rangpur.core.data.universalSeparator
import io.github.iamfacetheflames.rangpur.core.repository.Configuration
import io.github.iamfacetheflames.rangpur.core.repository.database.Database
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class CachedDirectories(
    private val database: Database,
    private val config: Configuration
) {

    private var deque: Deque<String> =
        LinkedList()
    private val cache: HashMap<String, Directory> =
        HashMap()

    fun getFullDirectoryPath(directoryUUID: String): String {
        deque = LinkedList()

        var current: Directory? = findDirectory(directoryUUID)
        while (current != null) {
            deque.push(current.name)
            current = getDirectory(current.parent)
        }

        val stringBuffer = StringBuffer()
        stringBuffer.append(config.getMusicDirectoryLocation())
        stringBuffer.append(File.separator)
        while (deque.isNotEmpty()) {
            stringBuffer.append(deque.pop())
            stringBuffer.append(universalSeparator)
        }

        return stringBuffer.toString()
    }

    fun getFullAudioPath(audio: Audio): String {
        val path = getFullDirectoryPath(
            audio.directoryUUID
        )
        return path + universalSeparator + audio.fileName
    }

    fun release() {
        cache.clear()
        deque.clear()
    }

    private fun getDirectory(directoryOnlyId: Directory?): Directory? {
        return if (directoryOnlyId == null) {
            null
        } else {
            return findDirectory(directoryOnlyId.uuid)
        }
    }

    fun findDirectory(directoryUUID: String): Directory? {
        val directory = cache[directoryUUID] ?: database.directories.getItem(directoryUUID)
        if (directory != null) {
            cache[directory.uuid] = directory
        }
        return directory
    }

}