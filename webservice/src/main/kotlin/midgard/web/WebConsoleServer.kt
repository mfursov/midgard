package midgard.web


import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ClosedSendChannelException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class ConsoleSession(val id: String, val charId: String)

interface WebConsoleServer {
    suspend fun memberJoin(member: String, socket: WebSocketSession)
    suspend fun memberLeft(member: String, socket: WebSocketSession)
    suspend fun onInput(member: String, input: Frame.Text)

    fun send2Char(member: String, message: String)
    fun nextLine(member: String): String?
}

class WebConsoleServerImpl : WebConsoleServer {

    val members = ConcurrentHashMap<String, MutableList<WebSocketSession>>()
    val inputQueue = LinkedList<String>()

    override suspend fun memberJoin(member: String, socket: WebSocketSession) {
        val list = members.computeIfAbsent(member) { CopyOnWriteArrayList<WebSocketSession>() }
        list.add(socket)
    }

    override suspend fun memberLeft(member: String, socket: WebSocketSession) {
        val connections = members[member]
        connections?.remove(socket)
    }

    override suspend fun onInput(member: String, input: Frame.Text) {
        inputQueue.add(input.readText())
    }

    override fun send2Char(member: String, message: String) {
        val allMembers = members.keys().toList()
        for (m in allMembers) {
            async { members[m]?.send(Frame.Text(message)) }
        }
    }

    override fun nextLine(member: String) = if (inputQueue.size > 0) inputQueue.removeFirst() else null

    suspend fun List<WebSocketSession>.send(frame: Frame) {
        forEach {
            try {
                it.send(frame.copy())
            } catch (t: Throwable) {
                try {
                    it.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                } catch (ignore: ClosedSendChannelException) {
                    // at some point it will get closed
                }
            }
        }
    }
}