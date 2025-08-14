package test.kotlin.io.github.djfx229.rangpur.ormlite

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.logger.Level
import com.j256.ormlite.logger.Logger
import com.j256.ormlite.support.ConnectionSource
import io.github.djfx229.rangpur.common.domain.database.Database
import io.github.djfx229.rangpur.common.data.database.OrmLiteDatabase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class OrmLiteDirectoriesTest {

    val tempDirectory = System.getProperty("java.io.tmpdir")
    val testDirectory = File(tempDirectory, "rangpur").also {
        if (it.exists()) {
            it.deleteRecursively()
        }
        it.mkdirs()
    }
    val sep = File.separatorChar
    val dbPath = "$testDirectory${sep}RangDatabaseTest.sqlite3"
    lateinit var database: Database

    @org.junit.jupiter.api.BeforeEach
    fun setUp() {
        Logger.setGlobalLogLevel(Level.ERROR)
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
        val directoryWithChilds = database.getBuilder().createDirectory(
            "dir 2",
            "/dir 2",
            null
        )
        val directoriesForTest = mutableListOf(
            database.getBuilder().createDirectory(
                "dir 1",
                "/dir 1",
                null
            ),
            directoryWithChilds,
            database.getBuilder().createDirectory(
                "sub dir 1",
                "/dir 2/sub dir 1",
                directoryWithChilds
            ),
            database.getBuilder().createDirectory(
                "sub dir 2",
                "/dir 2/sub dir 2",
                directoryWithChilds
            )
        )
        database.directories.update(
            directoriesForTest
        )
        assertEquals(directoriesForTest.size, database.directories.getAll().size, "test directories list size")
        assertEquals(2, database.directories.getOnlyRoot().size, "test root directories list size")
    }

}