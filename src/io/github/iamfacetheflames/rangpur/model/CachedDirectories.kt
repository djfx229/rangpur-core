package io.github.iamfacetheflames.rangpur.model

import io.github.iamfacetheflames.rangpur.data.Directory
import io.github.iamfacetheflames.rangpur.repository.Configuration
import io.github.iamfacetheflames.rangpur.repository.Database
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class CachedDirectories(val database: Database, val config: Configuration) {

    private var deque: Deque<String> =
        LinkedList()
    private val cache: HashMap<Long, Directory> =
        HashMap()

    fun getFullPath(directory: Directory): String {
        deque = LinkedList()

        var current: Directory? = directory
        while (current != null) {
            deque.push(current.name)
            current = getDirectory(current.parent)
        }

        val stringBuffer = StringBuffer()
        stringBuffer.append(config.getMusicDirectoryLocation())
        stringBuffer.append(File.separator)
        while (deque.isNotEmpty()) {
            stringBuffer.append(deque.pop())
            stringBuffer.append(File.separator)
        }

        return stringBuffer.toString()
    }

    fun release() {
        cache.clear()
        deque.clear()
    }

    private fun getDirectory(directoryOnlyId: Directory?): Directory? {
        return if (directoryOnlyId == null) {
            null
        } else {
            return findDirectory(directoryOnlyId.id)
        }
    }

    private fun findDirectory(directoryId: Long): Directory? {
        val directory = cache[directoryId] ?: database.getDirectory(directoryId)
        if (directory != null) {
            cache[directory.id] = directory
        }
        return directory
    }

}