package io.github.djfx229.rangpur.core.model

import io.github.djfx229.rangpur.feature.audio.domain.model.Audio
import io.github.djfx229.rangpur.core.data.Directory
import io.github.djfx229.rangpur.core.repository.Configuration
import io.github.djfx229.rangpur.core.repository.database.Database
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class CachedDirectoriesTest {
    private lateinit var cache: CachedDirectories
    private val databaseDirectories = mockk<Database.Directories>(relaxed = true)
    private val config = mockk<Configuration>(relaxed = true)

    private val libraryPath = "C:/Музыка"
    private val firstDirectoryId = "1"
    private val secondDirectoryId = "2"
    private val firstDirectoryRelativePath = "/drone/очень длинный кот - made in heaven/"
    private val secondDirectoryRelativePath = "/drone/"
    private val firstDirectory = mockk<Directory>(relaxed = true)
    private val secondDirectory = mockk<Directory>(relaxed = true)

    @BeforeEach
    fun setUp() {
        cache = CachedDirectories(databaseDirectories, config)
        every { config.getMusicDirectoryLocation() } answers { libraryPath }

        every { databaseDirectories.getItem(firstDirectoryId) } answers { firstDirectory }
        every { databaseDirectories.getItem(secondDirectoryId) } answers { secondDirectory }

        every { firstDirectory.locationInMusicDirectory } answers { firstDirectoryRelativePath }
        every { secondDirectory.locationInMusicDirectory } answers { secondDirectoryRelativePath }
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun getFullDirectoryPath() {
        // эти вызовы происходят впервые и приведут к запросу данных из БД
        assertEquals(
            libraryPath + firstDirectoryRelativePath,
            cache.getFullDirectoryPath(firstDirectoryId)
        )
        assertEquals(
            libraryPath + secondDirectoryRelativePath,
            cache.getFullDirectoryPath(secondDirectoryId)
        )

        // данные директории уже были запрошены ранее и должны вернуть данные из кеша
        assertEquals(
            libraryPath + firstDirectoryRelativePath,
            cache.getFullDirectoryPath(firstDirectoryId)
        )
        assertEquals(
            libraryPath + secondDirectoryRelativePath,
            cache.getFullDirectoryPath(secondDirectoryId)
        )

        verify(exactly = 1) { databaseDirectories.getItem(firstDirectoryId) }
        verify(exactly = 1) { databaseDirectories.getItem(secondDirectoryId) }
    }

    @Test
    fun getFullAudioPath() {
        val fileName = "Special Species Records - SSR022- очень длинный кот- made in heaven - 17 embee end.mp3"
        val expectedPath = libraryPath + firstDirectoryRelativePath + fileName
        val audio = mockk<Audio>(relaxed = true)
        every { audio.directoryUUID } answers { firstDirectoryId }
        every { audio.fileName } answers { fileName }

        val result = cache.getFullAudioPath(audio)

        assertEquals(expectedPath, result)
    }

    @Test
    fun release() {
        // эти вызовы происходят впервые и приведут к запросу данных из БД
        assertEquals(cache.getFullDirectoryPath(firstDirectoryId), libraryPath + firstDirectoryRelativePath)
        assertEquals(cache.getFullDirectoryPath(secondDirectoryId), libraryPath + secondDirectoryRelativePath)

        cache.release()

        // кэш очищен, поэтому вызовы снова приводят к запросу данных из БД
        assertEquals(cache.getFullDirectoryPath(firstDirectoryId), libraryPath + firstDirectoryRelativePath)
        assertEquals(cache.getFullDirectoryPath(secondDirectoryId), libraryPath + secondDirectoryRelativePath)

        verify(exactly = 2) { databaseDirectories.getItem(firstDirectoryId) }
        verify(exactly = 2) { databaseDirectories.getItem(secondDirectoryId) }
    }
}