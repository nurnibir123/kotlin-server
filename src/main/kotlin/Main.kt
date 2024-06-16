package org.example

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.*
import io.ktor.utils.io.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

// runBlocking here
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
        writeResponse(writeChannel, response)
    } finally {
        socket.close()
    }
}

suspend fun readRequest(readChannel: ByteReadChannel): HttpRequest {
    // read first line
    val firstLine = readChannel.readUTF8Line() ?: throw IllegalStateException("Empty Request")
    val firstLineSplit = firstLine.split(" ")
    if (firstLineSplit.size < 3)
        throw IllegalStateException("Invalid HTTP request")

    val method = firstLineSplit[0]
    val url = firstLineSplit[1]
    val protocolVersion = firstLineSplit[2]

    // read the headers
    val headersMap = mutableMapOf<String, String>()
    while (true) {
        val headerLine = readChannel.readUTF8Line() ?: break
        if (headerLine.isEmpty())
            break
        val headerLineSplit = headerLine.split(":", limit=2)
        if (headerLineSplit.size == 2)
            headersMap[headerLineSplit[0].trim()] = headerLineSplit[1].trim()
    }

    return HttpRequest(method, url, protocolVersion, headersMap)
}

suspend fun processRequest(request: HttpRequest) : String{
    return when (request.method) {
        "GET" -> handleGet(request)
        "POST" -> handlePost(request)
        "PUT" -> handlePut(request)
        "DELETE" -> handleDelete(request)
    }
}

suspend fun writeResponse(writeChannel: ByteWriteChannel, response: String) {
    writeChannel.writeStringUtf8(response)
}

fun httpResponse(statusCode: Int, statusMethod: String, body: String): String {
    return """
        HTTP/1.1 $statusCode $statusMethod
        Content-Type: text/plain
        Content-Length: ${body.length}
        
        $body
    """.trimIndent()
}

suspend fun handleGet(request: HttpRequest) : String {

}
