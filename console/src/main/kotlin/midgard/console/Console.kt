package midgard.console

import midgard.EventLoop
import midgard.action.CreateCharacterAction
import midgard.action.LinkCharacterAction
import midgard.event.NewCharacterCreatedEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject


suspend fun main(vararg args: String) {
    println("Midgard Console started.")

    ConsoleServer().start();

    println("Bye!")
}

class ConsoleServer : KoinComponent {
    val eventLoop by inject<EventLoop>()

    suspend fun start() {
        val characterName = "Odin"
        val charId = eventLoop.acceptOnce { (it as? NewCharacterCreatedEvent)?.charId }
        eventLoop.post(CreateCharacterAction(characterName)).await()
        eventLoop.post(LinkCharacterAction(charId.await())).await()
        var line: String?
        do {
            line = readLine()
            if (line == null) {
                break
            }
//            when(line) {
//                "n", "north" ->
//            }
        } while (line != "quit")

    }
}

