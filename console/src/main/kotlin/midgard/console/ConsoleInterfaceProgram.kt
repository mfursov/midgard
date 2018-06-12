package midgard.console

import midgard.*
import midgard.action.CreateCharacterAction
import midgard.action.LinkCharacterAction
import midgard.action.WalkAction
import midgard.db.Translator
import midgard.event.CharacterEntersEvent
import midgard.event.CharacterLeavesEvent
import midgard.event.NewCharacterCreatedEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

const val NAME = "Odin"

class ConsoleInterfaceProgram : Program(ProgramId("console-interface"), 1000), KoinComponent {
    private val console by inject<ConsoleInterface>()
    private val eventLoop by inject<EventLoop>()
    private val tr by inject<Translator>()

    private var state = ConnectionState.Started
    private lateinit var charId: CharacterId

    override fun onEvent(event: Event, world: World) {
        state = when (state) {
            ConnectionState.Started -> {
                println("Creating character")
                eventLoop.post(CreateCharacterAction(NAME))
                ConnectionState.WaitingCreate
            }
            ConnectionState.WaitingCreate -> {
                val charId = if (event is NewCharacterCreatedEvent && event.name == NAME) event.charId else null
                if (charId != null) {
                    this.charId = charId
                }
                if (charId == null) ConnectionState.WaitingCreate else ConnectionState.Created
            }
            ConnectionState.Created -> {
                println("Linking character")
                eventLoop.post(LinkCharacterAction(charId))
                ConnectionState.WaitingLink
            }
            ConnectionState.WaitingLink -> {
                if (!world.characters.containsKey(charId)) {
                    ConnectionState.WaitingLink
                } else {
                    println("You are online")
                    doLook(world)
                    ConnectionState.Playing
                }
            }
            ConnectionState.Playing -> {
                processEvent(event, world)
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

    private fun processEvent(event: Event, world: World) {
        when {
            event is CharacterLeavesEvent && event.charId == charId -> {
                val place = world.places[event.placeId] ?: return
                //todo: leaving
                send2Char(tr.tr(place.name))
            }
            event is CharacterEntersEvent && event.charId == charId -> {
                val place = world.places[event.placeId] ?: return
                //todo: entering
                send2Char(tr.tr(place.name))
            }
        }
    }

    private fun doLook(world: World) {
        val char = world.characters[charId] ?: return
        val place = world.places[char.placeId] ?: return
        send2Char(tr.tr(place.name))
        place.characters.filter { it != charId }
                .mapNotNull { world.characters[it] }
                .forEach { send2Char("${it.name} is here") }
    }

}