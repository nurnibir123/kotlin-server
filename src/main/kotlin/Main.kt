import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.*
import io.ktor.utils.io.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


// runBlocking here means
fun main() {
    val selectorManager = SelectorManager(Dispatchers.IO)
    runBlocking {
        startServer(selectorManager)
    }
}

// suspend functions are functions that can be paused and resumed
// at a later time without blocking the main thread
suspend fun startServer(selectorManager: SelectorManager) {
    val serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", 9002)
    println("Server is listening on 127.0.0.1:9002")

    while (true) {
        val socket = acceptConnection(serverSocket)
        handleClient(socket)
    }
}

suspend fun acceptConnection(serverSocket: io.ktor.network.sockets.ServerSocket): Socket {
    // since we have to wait for a connection to be accepted, this is a blocking operation
    // which will block the main thread, we need to switch its context to a thread on the
    // Dispatchers.IO thread pool which has threads for handling blocking IO operations
    return withContext(Dispatchers.IO) {
        serverSocket.accept()
    }
}

suspend fun handleClient(socket: Socket) {
    val readChannel = socket.openReadChannel()
    val writeChannel = socket.openWriteChannel()

    try {
        val request = readRequest(readChannel)
        println("Received request: $request")
        val response = processRequest(request)

    }
}

suspend fun readRequest(readChannel: ByteReadChannel): String {
    return ""
}

suspend fun processRequest(request: String) : String{

}

suspend fun writeResponse(writeChannel: ByteWriteChannel, response: String) : Boolean {
    return false
}
