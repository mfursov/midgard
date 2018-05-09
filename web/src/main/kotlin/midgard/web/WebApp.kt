package midgard.web

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.defaultResource
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.util.nextNonce
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.consumeEach
import org.koin.ktor.ext.inject
import org.koin.standalone.StandAloneContext.startKoin
import java.time.Duration

@Suppress("unused")
fun Application.main() {
    startKoin(listOf(appContext))

    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
    }

    val server: ChatServer by inject()

    routing {

        install(Sessions) {
            cookie<ChatSession>("SESSION")
        }

        intercept(ApplicationCallPipeline.Infrastructure) {
            if (call.sessions.get<ChatSession>() == null) {
                call.sessions.set(ChatSession(nextNonce()))
            }
        }

        get("/hello") {
            call.respond("Hello!")
        }

        webSocket("/ws") {
            val session = call.sessions.get<ChatSession>()
            if (session == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            server.memberJoin(session.id, this)

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        receivedMessage(server, session.id, frame.readText())
                    }
                }
            } finally {
                server.memberLeft(session.id, this)
            }
        }

        static {
            defaultResource("chat.html", "static")
            resources("static")
        }

    }
}

data class ChatSession(val id: String)

private suspend fun receivedMessage(server: ChatServer, id: String, command: String) {
    when {
        command.startsWith("/who") -> server.who(id)
        command.startsWith("/user") -> {
            val newName = command.removePrefix("/user").trim()
            when {
                newName.isEmpty() -> server.sendTo(id, "server::help", "/user [newName]")
                newName.length > 50 -> server.sendTo(id, "server::help", "new name is too long: 50 characters limit")
                else -> server.memberRenamed(id, newName)
            }
        }
        command.startsWith("/help") -> server.help(id)
        command.startsWith("/") -> server.sendTo(id, "server::help", "Unknown command ${command.takeWhile { !it.isWhitespace() }}")
        else -> server.message(id, command)
    }
}