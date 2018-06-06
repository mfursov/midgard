package midgard.console

import midgard.EventLoop
import midgard.instance.EventLoopImpl
import midgard.instance.instanceModule
import midgard.instance.instancePrograms
import org.koin.dsl.module.applicationContext
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject


fun consoleServerPrograms() = listOf(ConsoleInterfaceProgram())

val consoleServerModule = applicationContext {
    bean { instancePrograms().union(consoleServerPrograms()).toList() }
    bean<ConsoleInterface> { ConsoleServer }
}


fun main(vararg args: String) {
    StandAloneContext.startKoin(listOf(instanceModule, consoleServerModule))

    println("Midgard Console started.")

    ConsoleServer.start()

    println("Bye!")
}

enum class ConnectionState {
    Started,
    WaitingCreate,
    Created,
    WaitingLink,
    Playing
}

interface ConsoleInterface {
    fun nextLine(): String?
}

object ConsoleServer : KoinComponent, ConsoleInterface {
    private val eventLoop by inject<EventLoop>()
    private val inputLines = mutableListOf<String>()

    fun start() {
        (eventLoop as EventLoopImpl).start() //todo:
        while (true) {
            val line = readLine()
            if (line == "quit") {
                break
            }
            if (line == null) {
                Thread.sleep(1000)
            } else {
                synchronized(this) {
                    inputLines.add(line)
                }
            }
        }
        (eventLoop as EventLoopImpl).stop()
    }

    override fun nextLine(): String? {
        synchronized(this) {
            return if (inputLines.isEmpty()) null else inputLines.removeAt(0)
        }
    }

}

