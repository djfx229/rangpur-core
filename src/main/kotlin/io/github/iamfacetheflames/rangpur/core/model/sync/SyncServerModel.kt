package io.github.iamfacetheflames.rangpur.core.model.sync

import io.github.iamfacetheflames.rangpur.core.data.SyncInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

class SyncServerModel(
    private val handler: ServerHandler,
) {

    private var currentSocket: SocketChannel? = null
    private var serverSocketChannel: ServerSocketChannel? = null

    suspend fun start(
        host: String,
        port: Int = PORT,
        println: (String) -> Unit = ::println,
        listener: (SyncInfo) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        try {
            println("runServer() start $host : $port")
            val socket = ServerSocketChannel.open()
            socket.bind(InetSocketAddress(host, port))
            println("server sync: server running on port ${socket.socket().localPort}")
            serverSocketChannel = socket
            socket.accept().use { client ->
                currentSocket = client
                println("server sync: client connected : ${client.socket().inetAddress.hostAddress}")
                handler.connected(
                    SyncBridgeToClient(
                        clientSocket = client,
                        toClient = ObjectOutputStream(client.socket().getOutputStream()),
                        fromClient = ObjectInputStream(client.socket().getInputStream()),
                    ),
                    listener
                )
                client.close()
            }
        } catch (e: CancellationException) {
            serverSocketChannel?.close()
            currentSocket?.finishConnect()
            println(e.stackTraceToString())
        } catch (e: Exception) {
            println(e.stackTraceToString())
        } finally {
            currentSocket = null
        }
    }

    fun stop() {
        currentSocket?.finishConnect()
        serverSocketChannel?.close()
    }

}
