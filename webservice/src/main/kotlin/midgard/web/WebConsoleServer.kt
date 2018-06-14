package midgard.web


import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import kotlinx.coroutines.experimental.channels.ClosedSendChannelException
import midgard.CharacterId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class ConsoleSession(val id: String, val charId: String)

interface WebConsoleServer {
    suspend fun memberJoin(member: String, socket: WebSocketSession)
    suspend fun memberLeft(member: String, socket: WebSocketSession)

    suspend fun north(member: String)
    suspend fun south(member: String)
}

class WebConsoleServerImpl : WebConsoleServer {

    val members = ConcurrentHashMap<String, MutableList<WebSocketSession>>()

    override suspend fun memberJoin(member: String, socket: WebSocketSession) {
        val list = members.computeIfAbsent(member) { CopyOnWriteArrayList<WebSocketSession>() }
        list.add(socket)
    }

    override suspend fun memberLeft(member: String, socket: WebSocketSession) {
        val connections = members[member]
        connections?.remove(socket)
    }


    override suspend fun north(member: String) {
        members[member]?.send(Frame.Text("Got 'North'"))
    }

    override suspend fun south(member: String) {
        members[member]?.send(Frame.Text("Got 'South'"))
    }

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