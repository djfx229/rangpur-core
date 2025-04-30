package io.github.djfx229.rangpur.core.model

import io.github.djfx229.rangpur.core.data.Audio
import io.github.djfx229.rangpur.core.data.AudioInPlaylist
import java.io.File
import java.io.FileWriter

object PlaylistToFile {

    // don't forget call cachedDirs.release()
    fun exportPlaylistM3u8(name: String,
                           path: String,
                           audios: List<Audio>,
                           cachedDirs: io.github.djfx229.rangpur.core.model.CachedDirectories
    ): File {
        val lines = ArrayList<String>()
        lines.add("#EXTM3U\n")
        audios.forEach {
            val directoryPath = cachedDirs.getFullDirectoryPath(it.directoryUUID)
            lines.add(directoryPath + it.fileName + "\n")
        }

        val root = File(path)
        if (!root.exists()) {
            root.mkdirs()
        }
        val fileName = if (name.endsWith(".m3u8")) {
            name
        } else {
            "$name.m3u8"
        }
        val gpxfile = File(root, fileName)
        val writer = FileWriter(gpxfile)
        for (line in lines) {
            writer.append(line)
        }
        writer.flush()
        writer.close()
        return gpxfile
    }

    fun exportPlaylistTxt(
            name: String,
            path: String,
            audios: List<AudioInPlaylist>
    ): File {
        val lines = ArrayList<String>()
        audios.forEach {
            it.audio?.apply {
                lines.add("$artist - $title\n")
            }
        }

        val root = File(path)
        if (!root.exists()) {
            root.mkdirs()
        }
        val fileName = if (name.endsWith(".txt")) {
            name
        } else {
            "$name.txt"
        }
        val gpxfile = File(root, fileName)
        val writer = FileWriter(gpxfile)
        for (line in lines) {
            writer.append(line)
        }
        writer.flush()
        writer.close()
        return gpxfile
    }

}