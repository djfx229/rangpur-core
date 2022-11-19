package test.kotlin.io.github.iamfacetheflames.rangpur.core.model
import io.github.iamfacetheflames.rangpur.core.model.sync.ClientHandler
import io.github.iamfacetheflames.rangpur.core.model.sync.ServerHandler
import io.github.iamfacetheflames.rangpur.core.module.sync.SyncClient
import io.github.iamfacetheflames.rangpur.core.model.sync.SyncServerModel
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test

internal class SyncClientModelTest {

    private lateinit var syncModel: SyncModel

    /// Клиент должен успешно подсоединится к серверу
    @Test
    fun start() {
        @MockK
        lateinit var serverHandler: ServerHandler
        @MockK
        lateinit var clientHandler: ClientHandler

        val server = SyncServerModel(serverHandler)
        val client = SyncClient(clientHandler)

        runBlockingTest {

        }
        server.start("localhost")
    }

}