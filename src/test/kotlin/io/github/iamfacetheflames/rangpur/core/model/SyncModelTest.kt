package test.kotlin.io.github.iamfacetheflames.rangpur.core.model
import io.github.iamfacetheflames.rangpur.core.data.SyncInfo
import io.github.iamfacetheflames.rangpur.core.model.CachedDirectories
import io.github.iamfacetheflames.rangpur.core.model.sync.*
import io.github.iamfacetheflames.rangpur.core.repository.Configuration
import io.github.iamfacetheflames.rangpur.core.repository.database.Database
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.random.Random
import kotlin.test.assertContentEquals

/**
 * Набор тестов для проверки работы клиент-серверного взаимодействия (обмен данными и файлами).
 * Проверка непосредственно логики синхронизации находится в [SyncHandlersTest]
 */
internal class SyncModelTest {

    /**
     * Проверка соединения клиента с сервером
     */
    @Test
    fun connection() {
        val clientListener = mockk<(SyncInfo) -> Unit>(relaxed = true)
        val cachedDirectoriesClient = mockk<CachedDirectories>(relaxed = true)
        val database = mockk<Database>(relaxed = true)
        val clientHandler = spyk(
            ClientHandlerImpl(
                database,
                cachedDirectoriesFactory = {cachedDirectoriesClient},
                listener = clientListener,
            )
        )
        coEvery { clientHandler.receiveCommand(any(), any()) } coAnswers {
            val command = firstArg<String>()
            val bridge = secondArg<SyncBridgeToServer>()
            if (command == Command.GREETING)
            bridge.write(0)
        }
        val serverHandler = object : ServerHandler {
            override suspend fun connected(client: SyncBridge) {
                // получаем с клиента Command.START_SYNC
                val startCommand = client.read<String>()
                assertEquals(Command.START_SYNC, startCommand)
                // остальные команды и приём данных просто для проверки того что взаимодействие клиента
                // с сервером работает как надо
                client.write(Command.GREETING)
                client.read<Int>()
                client.write(Command.DONE)
            }
        }
        val server = SyncServerModel(serverHandler)
        val client = SyncClientModel(clientHandler)
        runTest {
            val host = "localhost"
            launch {
                server.start(host, 54287)
            }
            client.start(host, 54287)
            coVerify {
                clientHandler.receiveCommand(Command.GREETING, any())
                clientHandler.receiveCommand(Command.DONE, any())
            }
        }
    }

    /**
     * Проверка приёма файлов с сервера
     */
    @Test
    fun receiveFiles() {
        val clientListener = mockk<(SyncInfo) -> Unit>(relaxed = true)
        val cachedDirectoriesClient = mockk<CachedDirectories>(relaxed = true)
        val commandSendFile = "COMMAND_SEND_FILE"
        val fileContent: ByteArray = Random.nextBytes(1024 * 1024 * 10) // 10 MB
        val database = mockk<Database>(relaxed = true)
        val clientHandler = spyk(
            ClientHandlerImpl(
                database,
                cachedDirectoriesFactory = {cachedDirectoriesClient},
                listener = clientListener,
            )
        )
        coEvery { clientHandler.receiveCommand(commandSendFile, any()) } coAnswers {
            val bridge = secondArg<SyncBridgeToServer>()
            val fileForReceive = File.createTempFile(
                "rangpur",
                ".tmp"
            )
            val path = fileForReceive.absolutePath
            println("приём файла $path")
            val size = bridge.receiveFile(path)
            val file = File(path)
            assert(file.exists() && size == file.length())
            bridge.write(Status.OK)
            val resultArray = FileUtils.readFileToByteArray(fileForReceive)
            assertContentEquals(fileContent, resultArray)
            fileForReceive.delete()
        }
        val serverHandler = object : ServerHandler {
            override suspend fun connected(bridge: SyncBridge) {
                val startCommand = bridge.read<String>()
                assertEquals(Command.START_SYNC, startCommand)

                val fileForSend = File.createTempFile(
                    "rangpur",
                    ".tmp"
                )
                FileUtils.writeByteArrayToFile(fileForSend, fileContent)
                val path = fileForSend.absolutePath
                println("отправка файла $path")
                bridge.write(commandSendFile)
                bridge.sendFile(path)
                assertEquals(true, bridge.isStatusOk())
                bridge.done()
                fileForSend.delete()
            }
        }
        val server = SyncServerModel(serverHandler)
        val client = SyncClientModel(clientHandler)
        runTest {
            val host = "localhost"
            launch {
                server.start(host, 54288)
            }
            client.start(host, 54288)
        }
    }

}