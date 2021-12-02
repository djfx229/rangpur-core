package io.github.iamfacetheflames.rangpur.model

import io.github.iamfacetheflames.rangpur.data.Directory
import io.github.iamfacetheflames.rangpur.module.Configuration
import io.github.iamfacetheflames.rangpur.module.database.Database
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class CachedDirectories(val database: Database, val config: Configuration) {

    private var deque: Deque<String> =
        LinkedList()
    private val cache: HashMap<String, Directory> =
        HashMap()

    fun getFullPath(directoryUUID: String): String {
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