package midgard.console

import midgard.db.*
import midgard.instance.EventLoop
import midgard.instance.instanceModule
import midgard.instance.instancePrograms
import org.koin.dsl.module.applicationContext
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject


fun consoleServerPrograms() = listOf(ConsoleInterfaceProgram())

const val dataDir = "./db/data"

val consoleServerModule = applicationContext {
    bean<Translator> { MPropsTranslator(dataDir) }
    bean<Store> { LocalStore(dataDir, JsonFormat()) }
    bean { instancePrograms().union(consoleServerPrograms()).sortedBy { it.order }.toList() }
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
        eventLoop.start() //todo:
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
        eventLoop.stop()
    }

    override fun nextLine(): String? {
        synchronized(this) {
            return if (inputLines.isEmpty()) null else inputLines.removeAt(0)
        }
    }

}

