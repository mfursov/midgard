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

class ConsoleInterfaceProgram : Program(ProgramId("console-interface")), KoinComponent {
    private val console by inject<ConsoleInterface>()
    private val eventLoop by inject<EventLoop>()
    private val tr by inject<Translator>()

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
                    //todo: leaving
                    send2Char(tr.tr(place.name))
                }
                it is CharacterEntersEvent && it.charId == charId -> {
                    val place = world.places[it.placeId] ?: return
                    //todo: entering
                    send2Char(tr.tr(place.name))
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