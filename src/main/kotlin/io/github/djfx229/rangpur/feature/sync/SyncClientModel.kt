package io.github.djfx229.rangpur.feature.sync

import io.github.djfx229.rangpur.feature.sync.domain.model.SyncInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

class SyncClientModel(
    private val handler: ClientHandler,
) {

    private var serverSocket: SocketChannel? = null

    suspend fun start(
        host: String = "localhost",
        port: Int = PORT,
        listener: (SyncInfo) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        println("runClient() start $host : $port")
        SocketChannel.open(
            InetSocketAddress(
                host,
                port
            )
        ).use { server: SocketChannel ->
            serverSocket = server
            println("runClient() get server")
            val serverStreams = SyncBridgeToServer(
                serverSocket = server,
            )
            handler.connected(serverStreams, listener)
            server.close()
        }
    }

    fun stop() {
        serverSocket?.finishConnect()
    }

}