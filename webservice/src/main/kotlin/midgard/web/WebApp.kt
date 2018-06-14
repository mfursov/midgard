package midgard.web

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.defaultResource
import io.ktor.content.resource
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
import io.ktor.sessions.*
import io.ktor.util.nextNonce
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.consumeEach
import midgard.Program
import midgard.instance.instanceModule
import midgard.instance.instancePrograms
import org.koin.dsl.module.applicationContext
import org.koin.ktor.ext.inject
import org.koin.standalone.StandAloneContext
import java.time.Duration

fun webServerPrograms() = listOf<Program>()

val webServerModule = applicationContext {
    bean { instancePrograms().union(webServerPrograms()).sortedBy { it.order }.toList() }
    bean<ChatServer> { ChatServerImpl() }
    bean<WebConsoleServer> { WebConsoleServerImpl() }
}

@Suppress("unused")
fun Application.main() {
    StandAloneContext.startKoin(listOf(instanceModule, webServerModule))

    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(10)
    }

    val chatServer: ChatServer by inject()
    val consoleServer: WebConsoleServer by inject()

    routing {

        install(Sessions) {
            cookie<ChatSession>("CHAT_SESSION")
//            cookie<ConsoleSession>("CONSOLE_SESSION")
        }

        intercept(ApplicationCallPipeline.Infrastructure) {
            if (call.sessions.get<ChatSession>() == null) {
                call.sessions.set(ChatSession(nextNonce()))
            }
//            if (call.sessions.get<ConsoleSession>() == null) {
//                call.sessions.set(ConsoleSession(nextNonce(), "todo-char-id"))
//            }
        }

        get("/hello") { call.respond("Hello!") }

        webSocket("/chat") {
            val session = call.sessions.get<ChatSession>()
            if (session == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            chatServer.memberJoin(session.id, this)

            try {
                incoming.consumeEach { it ->
                    if (it is Frame.Text) receivedChatMessage(chatServer, session.id, it.readText())
                }
            } finally {
                chatServer.memberLeft(session.id, this)
            }
        }

//        webSocket("/console") {
//            val session = call.sessions.get<ConsoleSession>()
//            if (session == null) {
//                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
//                return@webSocket
//            }
//
//            consoleServer.memberJoin(session.id, this)
//
//            try {
//                incoming.consumeEach {
//                    if (it is Frame.Text) receivedConsoleMessage(consoleServer, session.id, it.readText())
//                }
//            } finally {
//                consoleServer.memberLeft(session.id, this)
//            }
//        }
        static {
            defaultResource("chat.html", "static")
            resource("console.html", "console.html", "static")
            resources("static")
        }
    }
}

private suspend fun receivedChatMessage(server: ChatServer, id: String, command: String) {
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

private suspend fun receivedConsoleMessage(server: WebConsoleServer, id: String, command: String) {
    when (command) {
        "n" -> server.north(id)
        "s" -> server.south(id)
    }
}
