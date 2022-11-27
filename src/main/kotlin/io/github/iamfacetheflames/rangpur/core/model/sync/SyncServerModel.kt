package io.github.iamfacetheflames.rangpur.core.model.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class SyncServerModel(
    private val handler: ServerHandler
) {

    private var currentSocket: SocketChannel? = null

    suspend fun start(
        host: String,
        port: Int = PORT,
        println: (String) -> Unit = ::println,
    ) = withContext(Dispatchers.IO) {
        try {
            println("runServer() start $host : $port")
            val socket = ServerSocketChannel.open()
            socket.bind(InetSocketAddress(host, port))
            println("server sync: server running on port ${socket.socket().localPort}")
            socket.accept().use { client ->
                currentSocket = client
                println("server sync: client connected : ${client.socket().inetAddress.hostAddress}")
                handler.connected(
                    SyncBridgeToClient(
                        clientSocket = client,
                        toClient = ObjectOutputStream(client.socket().getOutputStream()),
                        fromClient = ObjectInputStream(client.socket().getInputStream()),
                    )
                )
                client.close()
            }
        } catch (e: Exception) {
            println(e.stackTraceToString())
        } finally {
            currentSocket = null
        }
    }

    fun stopServer() {
        currentSocket?.finishConnect()
    }

}