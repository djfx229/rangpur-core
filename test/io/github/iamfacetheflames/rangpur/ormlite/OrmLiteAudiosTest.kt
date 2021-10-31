package io.github.iamfacetheflames.rangpur.ormlite

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.logger.Log
import com.j256.ormlite.logger.Logger
import com.j256.ormlite.support.ConnectionSource
import io.github.iamfacetheflames.rangpur.data.Filter
import io.github.iamfacetheflames.rangpur.repository.database.Database
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.sql.Date
import java.util.*

internal class OrmLiteAudiosTest {

    val dbPath = "${File(".").canonicalPath}/RangDatabaseTest.sqlite3"
    lateinit var database: Database

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        Logger.setGlobalLogLevel(Log.Level.ERROR)
        val url = "jdbc:sqlite:$dbPath"
        val source: ConnectionSource = JdbcConnectionSource(url, "", "")
        database = OrmLiteDatabase(source)
        assertTrue(
            File(dbPath).exists()
        )
    }

    @org.junit.jupiter.api.AfterEach
    fun tearDown() {
        File(dbPath).delete()
    }

    @Test
    fun createAndGetAll() {
        database.saveDirectories(
            mutableListOf(
                database.getBuilder().createDirectory(
                    "dir 1",
                    "/root/dir/1",
                    null
                )
            )
        )
        val directory = database.getDirectories().first()
        val audioBeforeCreate = mutableListOf(
            database.getBuilder().createAudio(
                directory,
                "RESET! - We're alive.mp3",
                "RESET!",
                "We're alive",
                Date(
                    Date().time
                )
            ),
            database.getBuilder().createAudio(
                directory,
                "Fake Blood - Mars.mp3",
                "Fake Blood",
                "Mars",
                Date(
                    Date().time
                )
            )
        )

        database.audios.create(
            audioBeforeCreate
        )
        val audioAfterCreated = database.audios.getAll()

        for (audio in audioBeforeCreate) {
            audio.apply {
                val isExistInDatabase = audioAfterCreated.contains(audio)
                val result = "check audio '$fileName' -- isExistInDatabase == $isExistInDatabase"
                assertTrue(isExistInDatabase, result)
            }
        }
    }

    @Test
    fun getFiltered() {
        database.saveDirectories(
            mutableListOf(
                database.getBuilder().createDirectory(
                    "dir 1",
                    "/root/dir/1",
                    null
                ),
                database.getBuilder().createDirectory(
                    "dir 2",
                    "/root/dir/2",
                    null
                )
            )
        )
        val directoryFirst = database.getDirectories().first()
        val directoryLast = database.getDirectories().last()
        val audio2010 = mutableListOf(
            database.getBuilder().createAudio(
                directoryFirst,
                "RESET! - We're alive.mp3",
                "RESET!",
                "We're alive",
                Date.valueOf("2010-09-01")
            ),
            database.getBuilder().createAudio(
                directoryLast,
                "FastFoot feat. Stereotoxic - Tango in Moscow.mp3",
                "FastFoot feat. Stereotoxic",
                "Tango in Moscow",
                Date.valueOf("2010-09-01")
            )
        )
        val forTestSearch = database.getBuilder().createAudio(
            directoryFirst,
            "Moe Shop - Love Me.mp3",
            "Moe Shop",
            "Love Me",
            Date.valueOf("2016-09-01")
        )
        val audio2016 = mutableListOf(
            forTestSearch,
            database.getBuilder().createAudio(
                directoryLast,
                "Hibiki - 愛してるFOREVER.mp3",
                "Hibiki",
                "愛してるFOREVER",
                Date.valueOf("2016-09-01")
            )
        )
        val audio2021 = mutableListOf(
            database.getBuilder().createAudio(
                directoryFirst,
                "Primary 1 - Hold Me Down (Foamo Remix).mp3",
                "Primary 1",
                "Hold Me Down (Foamo Remix)",
                Date.valueOf("2021-09-01")
            ),
            database.getBuilder().createAudio(
                directoryLast,
                "IGORRR - VERY NOISE.mp3",
                "IGORRR",
                "VERY NOISE",
                Date.valueOf("2021-09-01")
            )
        )
        val audioBeforeCreate = audio2010 + audio2016 + audio2021
        database.audios.create(audioBeforeCreate)
        val audioAfterCreated = database.audios.getAll()

        for (audio in audioBeforeCreate) {
            audio.apply {
                val isExistInDatabase = audioAfterCreated.contains(audio)
                val result = "check audio '$fileName' -- isExistInDatabase == $isExistInDatabase"
                assertTrue(isExistInDatabase, result)
            }
        }

        database.audios.getFiltered(
            Filter().apply {
                dateList.add("2010")
            }
        ).forEach { audio ->
            audio.apply {
                val isExistInDatabase = audio2010.contains(audio)
                val result = "check audio '$fileName' -- isExistInDatabase == $isExistInDatabase"
                assertTrue(isExistInDatabase, result)
            }
        }
        database.audios.getFiltered(
            Filter().apply {
                dateList.add("2016")
            }
        ).forEach { audio ->
            audio.apply {
                val isExistInDatabase = audio2016.contains(audio)
                val result = "check audio '$fileName' -- isExistInDatabase == $isExistInDatabase"
                assertTrue(isExistInDatabase, result)
            }
        }
        database.audios.getFiltered(
            Filter().apply {
                dateList.add("2021")
            }
        ).forEach { audio ->
            audio.apply {
                val isExistInDatabase = audio2021.contains(audio)
                val result = "check audio '$fileName' -- isExistInDatabase == $isExistInDatabase"
                assertTrue(isExistInDatabase, result)
            }
        }

        val searchFilter = Filter().apply {
            searchRequest = forTestSearch.artist!!
        }
        val testSearch = database.audios.getFiltered(searchFilter)
        assertTrue(testSearch.size == 1 && testSearch.first() == forTestSearch, "test search")
    }

}