package io.github.iamfacetheflames.rangpur.core.model.sync

import io.github.iamfacetheflames.rangpur.core.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

class SyncClientModel(
    private val handler: ClientHandler
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
            handler.connected(serverStreams)
            server.close()
            listener(SyncInfoFinished)
        }
    }

    fun stop() {
        serverSocket?.finishConnect()
    }

}