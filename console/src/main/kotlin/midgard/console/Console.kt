package midgard.console

import midgard.ActionId
import midgard.EventId
import midgard.EventLoop
import midgard.Program
import midgard.ProgramId
import midgard.World
import midgard.action.CreateCharacterAction
import midgard.action.LinkCharacterAction
import midgard.action.WalkAction
import midgard.area.model.Character
import midgard.area.model.CharacterId
import midgard.area.model.Direction
import midgard.area.model.ExitInfo
import midgard.area.model.Place
import midgard.area.model.PlaceId
import midgard.buildActionHandlers
import midgard.event.CharacterEntersEvent
import midgard.event.CharacterLeavesEvent
import midgard.event.NewCharacterCreatedEvent
import midgard.impl.EventLoopImpl
import midgard.program.greeting.GuardGreetingProgram
import midgard.util.RandomGenerator
import org.koin.dsl.module.applicationContext
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject

//TODO: rework to Kotlin version of atomic number.
private var actionIdCounter = 0
private var eventIdCounter = 0
private var characterIdCounter = 0

val appContext = applicationContext {
    bean { RandomGenerator(1) }
    bean { loadWorld() }
    bean<EventLoop> { EventLoopImpl() }
    bean { buildActionHandlers() }
    bean { listOf(ConsoleInterfaceProgram(), GuardGreetingProgram()) }
    bean<ConsoleInterface> { ConsoleServer }
    factory { ActionId("a-" + (++actionIdCounter)) }
    factory { EventId("e-" + (++eventIdCounter)) }
    factory { CharacterId("c-" + (++characterIdCounter)) }
}

private fun loadWorld(): World {
    val world = World()
    world.places.putAll(generatePlaces())
    val charId = CharacterId("${++characterIdCounter}")
    val place = world.places.values.first()
    val char = Character(charId, "Guard", place.id)
    world.characters[charId] = char
    place.characters.add(charId)
    char.programData["greeter"] = "yes"
    return world
}


fun main(vararg args: String) {
    StandAloneContext.startKoin(listOf(appContext))

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

const val NAME = "Odin"

class ConsoleInterfaceProgram : Program(ProgramId("console-interface")), KoinComponent {
    private val console by inject<ConsoleInterface>()
    private val eventLoop by inject<EventLoop>()

    private var state = ConnectionState.Started
    private lateinit var charId: CharacterId

    override fun tick(world: World) {
        state = when (state) {
            ConnectionState.Started -> {
                eventLoop.post(CreateCharacterAction(NAME))
                ConnectionState.WaitingCreate
            }
            ConnectionState.WaitingCreate -> {
                val charId = world.events.mapNotNull { it as? NewCharacterCreatedEvent }
                        .filter { it.name == NAME }.map { it.charId }.firstOrNull()
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
                if (!world.characters.containsKey(charId)) {
                    ConnectionState.WaitingLink
                } else {
                    doLook(world)
                    ConnectionState.Playing
                }
            }
            ConnectionState.Playing -> {
                processEvents(world)
                processInput(world)
            }
        }
    }

    private fun send2Char(text: String) {
        println(text)
    }

    private fun processInput(world: World): ConnectionState {
        val line = console.nextLine() ?: return ConnectionState.Playing
        when (line) {
            "l", "look" -> doLook(world) //todo: action?
            "n", "north" -> eventLoop.post(WalkAction(charId, Direction.North))
            "s", "south" -> eventLoop.post(WalkAction(charId, Direction.South))
            else -> send2Char("Huh?")
        }
        return ConnectionState.Playing
    }

    private fun processEvents(world: World) {
        world.events.forEach {
            when {
                it is CharacterLeavesEvent && it.charId == charId -> {
                    val place = world.places[it.placeId] ?: return
                    send2Char("Leaving ${place.name}")
                }
                it is CharacterEntersEvent && it.charId == charId -> {
                    val place = world.places[it.placeId] ?: return
                    send2Char("Entering ${place.name}")
                }
            }
        }
    }

    private fun doLook(world: World) {
        val char = world.characters[charId] ?: return
        val place = world.places[char.placeId] ?: return
        send2Char(place.name)
        place.characters.filter { it != charId }
                .mapNotNull { world.characters[it] }
                .forEach { send2Char("${it.name} is here") }
    }

}

fun generatePlaces(): Map<PlaceId, Place> {
    val places = mutableListOf<Place>()
    val p1 = Place(PlaceId("place-1"), "Place 1")
    val p2 = Place(PlaceId("place-2"), "Place 2")
    p1.exits[Direction.North] = ExitInfo(p2.id)
    p2.exits[Direction.South] = ExitInfo(p1.id)
    places.add(p1)
    places.add(p2)
    return places.associateBy({ it.id })
}

