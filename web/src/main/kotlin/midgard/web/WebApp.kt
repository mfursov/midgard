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
import midgard.World
import midgard.Character
import midgard.CharacterId
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject
import java.time.Duration

@Suppress("unused")
fun Application.main() {
//    startKoin(listOf(appContext, webAppContext))

    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
    }

//    val server: ChatServer = get()
    val server: ChatServer by inject()

    routing {

        install(Sessions) {
            cookie<CharacterSession>("SESSION")
        }

        intercept(ApplicationCallPipeline.Infrastructure) {
            if (call.sessions.get<CharacterSession>() == null) {
                val midgard = get<World>()
                val place = midgard.places.values.first()
                val ch = Character(CharacterId("char-1"), "name", place.id)
                place.characters.add(ch.id)
                midgard.characters[ch.id] = ch
                call.sessions.set(CharacterSession(nextNonce(), ch.id))
            }
        }

        get("/hello") {
            call.respond("Hello!")
        }

        webSocket("/ws") {
            val session = call.sessions.get<CharacterSession>()
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
                val midgard = get<World>()
                val ch = midgard.characters[session.charId]!!
                val place = midgard.places[ch.placeId]!!
                place.characters.remove(ch.id)
                midgard.characters.remove(ch.id)
            }
        }

        static {
            defaultResource("chat.html", "static")
            resources("static")
        }

    }
}

data class CharacterSession(val id: String, val charId: CharacterId)

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