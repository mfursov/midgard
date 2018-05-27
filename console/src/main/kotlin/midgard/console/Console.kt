package midgard.console

import midgard.EventLoop
import midgard.Program
import midgard.World
import midgard.action.CreateCharacterAction
import midgard.action.LinkCharacterAction
import midgard.action.WalkAction
import midgard.appContext
import midgard.area.model.CharacterId
import midgard.area.model.Direction
import midgard.event.NewCharacterCreatedEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject


fun main(vararg args: String) {
    StandAloneContext.startKoin(listOf(appContext))

    println("Midgard Console started.")

    ConsoleServer().start()

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

class ConsoleServer : KoinComponent, ConsoleInterface {
    val eventLoop by inject<EventLoop>()
    val inputLines = mutableListOf<String>()

    fun start() {
        eventLoop.add(ConsoleInterfaceProgram(this)) //todo: inject?
        eventLoop.start()
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

const val NAME = "Odin"

class ConsoleInterfaceProgram(val console: ConsoleInterface) : Program, KoinComponent {
    val eventLoop by inject<EventLoop>()
    var state = ConnectionState.Started
    lateinit var charId: CharacterId

    override fun tick(world: World) {
        state = when (state) {
            ConnectionState.Started -> {
                eventLoop.post(CreateCharacterAction(NAME))
                ConnectionState.WaitingCreate
            }
            ConnectionState.WaitingCreate -> {
                val charId = findCharacter(world, false)
                if (charId != null) {
                    this.charId = charId
                }
                if (charId == null) ConnectionState.WaitingCreate else ConnectionState.Created
            }
            ConnectionState.Created -> {
                eventLoop.post(LinkCharacterAction(charId))
                ConnectionState.WaitingLink
            }
            ConnectionState.WaitingLink -> {
                val charId = findCharacter(world, true)
                if (charId == null) ConnectionState.WaitingLink else ConnectionState.Playing
            }
            ConnectionState.Playing -> play()
        }
    }

    fun findCharacter(world: World, online: Boolean): CharacterId? {
        return world.events
                .mapNotNull { e -> e as? NewCharacterCreatedEvent }
                .mapNotNull { e -> if (online) world.characters[e.charId] else world.offlineCharacters[e.charId] }
                .filter { it.name == NAME }
                .map { c -> c.id }
                .firstOrNull()
    }

    fun play(): ConnectionState {
        val line = console.nextLine() ?: return ConnectionState.Playing
        when (line) {
            "n", "north" -> eventLoop.post(WalkAction(charId, Direction.North))
            "s", "south" -> eventLoop.post(WalkAction(charId, Direction.South))
            else -> println("Huh?")
        }
        return ConnectionState.Playing
    }
}

