package io.github.iamfacetheflames.rangpur.core.model.sync

import io.github.iamfacetheflames.rangpur.core.data.ClientSyncInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class SyncServerModel(
    private val handler: ServerHandler
) {

    private var currentSocket: ServerSocketChannel? = null

    suspend fun start(
        host: String,
        port: Int = PORT,
        println: (String) -> Unit = ::println,
        listener: (ClientSyncInfo) -> Unit = {},
    ) = withContext(Dispatchers.IO) {
        try {
            println("runServer() start $host : $port")
            val socket = ServerSocketChannel.open()
            currentSocket = socket
            socket.bind(InetSocketAddress(host, port))
            println("server sync: server running on port ${socket.socket().localPort}")
            socket.accept().use { client: SocketChannel ->
                println("server sync: client connected : ${client.socket().inetAddress.hostAddress}")
                handler.clientConnected(client)
            }
        } catch (e: Exception) {
            println(e.stackTraceToString())
        } finally {
            currentSocket = null
        }
    }

    fun stopServer() {
        currentSocket?.close()
    }

}

interface ServerHandler {
    suspend fun clientConnected(client: SocketChannel)
}