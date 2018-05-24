package midgard.console

import midgard.EventLoop
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject


fun main(vararg args: String) {
    println("Midgard Console started.")

    ConsoleServer().start();

    println("Bye!")
}

class ConsoleServer : KoinComponent {
    val eventLoop by inject<EventLoop>()

    fun start() {
        //todo: eventLoop.postAction(LinkCharacterAction())
        var line: String?
        do {
            line = readLine()
            if (line == null) {
                break;
            }
//            when(line) {
//                "n", "north" ->
//            }
        } while (line != "quit")

    }
}