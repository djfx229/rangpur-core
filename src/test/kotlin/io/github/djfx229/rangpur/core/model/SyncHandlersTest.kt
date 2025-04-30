package io.github.djfx229.rangpur.core.model

import io.github.djfx229.rangpur.core.data.*
import io.github.djfx229.rangpur.core.model.sync.*
import io.github.djfx229.rangpur.core.repository.database.Database
import io.mockk.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


internal class SyncHandlersTest {

    private fun createTestFile(parentDirectory: File, fileName: String): File {
        val file = File(parentDirectory, fileName)
        file.bufferedWriter().use { out ->
            out.write(fileName)
        }
        return file
    }

    private fun compareFilesInDirectories(directory1: File, directory2: File) {
        val files1 = directory1.listFiles()
        val files2 = directory2.listFiles()
        assertEquals(files1.size, files2.size)
        files1.forEach { directory1File ->
            val directory2File = files2.findLast { it.name == directory1File.name }
            assertNotNull(directory2File)
            assertTrue(FileUtils.contentEquals(directory1File, directory2File))
        }
    }

    /**
     * Проверка, что ClientHandlerImpl и ServerHandlerImpl способны синхронизироваться друг с другом.
     * Имитация ситуации, когда клиент синхронизируется в первый раз и у него нет данных в БД.
     */
    @Test
    fun syncForEmptyClient() {
        val cachedDirectoriesServer = mockk<CachedDirectories>(relaxed = true)
        val cachedDirectoriesClient = mockk<CachedDirectories>(relaxed = true)
        val databaseServer = mockk<Database>(relaxed = true)
        val databaseClient = mockk<Database>(relaxed = true)

        fun syncDirectories() {
            val serverDirectories = listOf<Directory>(
                TestDirectory(
                    name = "ambient",
                    locationInMusicDirectory = "/ambient/",
                ),
            )
            every {
                databaseServer.directories.getAll()
            } returns serverDirectories
            every {
                databaseClient.directories.update(any())
            } coAnswers {
                val result = firstArg<List<Directory>>()
                assertEquals(serverDirectories.size, result.size)
                result.forEach { clientDirectory ->
                    val findedDir = serverDirectories.firstOrNull { it.uuid == clientDirectory.uuid }
                    assertTrue(clientDirectory.equalsAllFields(findedDir))
                }
            }
        }

        fun syncAudios() {
            val content = "Stillgazer - What I Want"
            val fileForSend = File.createTempFile(
                "rangpur-for-send",
                ".tmp"
            )
            println("fileForSend = $fileForSend")
            val fileForReceive = File.createTempFile(
                "rangpur-for-receive",
                ".tmp"
            )
            println("fileForReceive = $fileForReceive")
            fileForSend.bufferedWriter().use { out ->
                out.write(content)
            }
            val serverAudios = listOf<Audio>(
                TestAudio(
                    fileName = fileForSend.name,
                    artist = "Stillgazer",
                    title = "What I Want",
                )
            )
            every {
                cachedDirectoriesServer.getFullAudioPath(any())
            } returns fileForSend.absolutePath
            every {
                cachedDirectoriesClient.getFullAudioPath(any())
            } returns fileForReceive.absolutePath
            every {
                databaseServer.audios.getAll()
            } returns serverAudios
            every {
                databaseClient.audios.create(any())
            } coAnswers {
                val result = firstArg<List<Audio>>()
                val originalFile = serverAudios.find { it.uuid == result.first().uuid }
                assertTrue(result.first().equalsAllFields(originalFile))
                fileForReceive.inputStream().bufferedReader().use {
                    assertEquals(it.readText(), content)
                }
            }
        }

        syncDirectories()
        syncAudios()

        val serverHandler = ServerHandlerImpl(
            databaseServer,
            cachedDirectoriesFactory = {cachedDirectoriesServer},
        )
        val clientHandler = ClientHandlerImpl(
            databaseClient,
            cachedDirectoriesFactory = {cachedDirectoriesClient},
        )
        val server = SyncServerModel(serverHandler)
        val client = SyncClientModel(clientHandler)
        runTest {
            val host = "localhost"
            launch {
                server.start(host, 54289)
            }
            launch {
                client.start(host, 54289)
            }
        }
    }

    /**
     * Проверка, что ClientHandlerImpl и ServerHandlerImpl способны синхронизироваться друг с другом.
     * Для ситуации когда у клиента уже присутствуют данные в БД.
     */
    @Test
    fun syncForNotEmptyClient() {
        val cachedDirectoriesServer = mockk<CachedDirectories>(relaxed = true)
        val cachedDirectoriesClient = mockk<CachedDirectories>(relaxed = true)
        val databaseServer = mockk<Database>(relaxed = true)
        val databaseClient = mockk<Database>(relaxed = true)

        val tempDirectory = System.getProperty("java.io.tmpdir")
        if (tempDirectory.isBlank()) {
            throw IllegalAccessError("Отсутствует путь до временной директории")
        }
        val libraryDirServer = File(tempDirectory, "rangpurLibraryDirServer").also {
            if (it.exists()) {
                it.deleteRecursively()
            }
            it.mkdirs()
        }
        val libraryDirClient = File(tempDirectory, "rangpurLibraryDirClient").also {
            if (it.exists()) {
                it.deleteRecursively()
            }
            it.mkdirs()
        }
        println("path to library server ${libraryDirServer.absolutePath}")
        println("path to library client ${libraryDirClient.absolutePath}")

        fun syncDirectories() {
            val clientDirectories = listOf<Directory>(
                TestDirectory(
                    name = "ambient",
                    locationInMusicDirectory = "/ambient/",
                ),
                TestDirectory(
                    name = "drone",
                    locationInMusicDirectory = "/drone/",
                ),
            )
            val serverDirectories = clientDirectories + listOf<Directory>(
                TestDirectory(
                    name = "fidget house",
                    locationInMusicDirectory = "/fidget house/",
                ),
            )
            every {
                databaseServer.directories.getAll()
            } returns serverDirectories
            every {
                databaseClient.directories.getAll()
            } returns clientDirectories
            every {
                databaseClient.directories.update(any())
            } coAnswers {
                val result = firstArg<List<Directory>>()
                assertEquals(serverDirectories.size - clientDirectories.size, result.size)
                result.forEach { clientDirectory ->
                    val findedDir = serverDirectories.firstOrNull { it.uuid == clientDirectory.uuid }
                    assertTrue(clientDirectory.equalsAllFields(findedDir))
                }
            }
        }

        fun syncAudios() {
            val file1 = createTestFile(libraryDirServer, "1_2 Orchestra - March of Dissent.mp3")
            val file2 = createTestFile(libraryDirServer, "Stillgazer - What I Want.mp3")
            val file3 = createTestFile(libraryDirServer, "Pat Metheny Group - Last Train Home.mp3")
            val file4 = createTestFile(libraryDirServer, "SUI UZI - Hide & Seek.mp3")
            createTestFile(libraryDirClient, "1_2 Orchestra - March of Dissent.mp3")
            createTestFile(libraryDirClient, "Stillgazer - What I Want.mp3")
            val audio1 = TestAudio(
                fileName = file1.name,
                artist = "1_2 Orchestra",
                title = "March of Dissent",
            )
            val audio2 = TestAudio(
                fileName = file2.name,
                artist = "Stillgazer",
                title = "What I Want",
            )
            val audio3 = TestAudio(
                fileName = file3.name,
                artist = "Pat Metheny Group",
                title = "Last Train Home",
            )
            val audio4 = TestAudio(
                fileName = file4.name,
                artist = "SUI UZI",
                title = "Hide & Seek",
            )
            val serverAudios = listOf<Audio>(
                audio1,
                audio2,
                audio3,
                audio4,
            )
            val clientAudios = listOf(
                audio1,
                audio2,
            )

            every {
                cachedDirectoriesServer.getFullAudioPath(any())
            } answers {
                val audio = firstArg<Audio>()
                when(audio) {
                    audio1 -> file1.absolutePath
                    audio2 -> file2.absolutePath
                    audio3 -> file3.absolutePath
                    else -> file4.absolutePath
                }
            }
            every {
                cachedDirectoriesClient.getFullAudioPath(any())
            } answers {
                val audio = firstArg<Audio>()
                when(audio.uuid) {
                    audio3.uuid -> File(libraryDirClient, file3.name).absolutePath
                    else -> File(libraryDirClient, file4.name).absolutePath
                }
            }
            every {
                databaseServer.audios.getAll()
            } returns serverAudios
            every {
                databaseClient.audios.getAll()
            } returns clientAudios
            every {
                databaseClient.audios.create(any())
            } coAnswers {
                val result = firstArg<List<Audio>>()
                assertNull(
                    clientAudios.find { it.uuid == result.first().uuid },
                    "Клиент не должен сохранять файлы которые уже есть в фонотеке"
                )
                val originalFile = serverAudios.find { it.uuid == result.first().uuid }
                assertTrue(result.first().equalsAllFields(originalFile))
            }
        }

        runTest {
            try {
                syncDirectories()
                syncAudios()

                val serverHandler = ServerHandlerImpl(
                    databaseServer,
                    cachedDirectoriesFactory = {cachedDirectoriesServer},
                )
                val clientHandler = ClientHandlerImpl(
                    databaseClient,
                    cachedDirectoriesFactory = {cachedDirectoriesClient},
                )
                val server = SyncServerModel(serverHandler)
                val client = SyncClientModel(clientHandler)

                val host = "localhost"
                launch {
                    server.start(host, 54290)
                }
                client.start(host, 54290)
                compareFilesInDirectories(libraryDirServer, libraryDirClient)
            } finally {
                libraryDirServer.deleteRecursively()
                libraryDirClient.deleteRecursively()
            }
        }
    }

}