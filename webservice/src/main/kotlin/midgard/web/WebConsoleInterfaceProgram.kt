package midgard.web

import midgard.*
import midgard.action.CreateCharacterAction
import midgard.action.LinkCharacterAction
import midgard.action.WalkAction
import midgard.db.Translator
import midgard.event.CharacterEntersEvent
import midgard.event.CharacterLeavesEvent
import midgard.event.NewCharacterCreatedEvent
import midgard.event.SayEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

const val NAME = "Odin"

enum class WebConnectionState {
    Started,
    WaitingCreate,
    Created,
    WaitingLink,
    Playing
}

class WebConsoleInterfaceProgram : Program(ProgramId("web-console-interface"), 1000), KoinComponent {
    private val tr by inject<Translator>()
    private val server by inject<WebConsoleServer>()

    private var state = WebConnectionState.Started
    private lateinit var charId: CharacterId

    override fun onEvent(event: Event, world: World) {
        state = when (state) {
            WebConnectionState.Started -> {
                println("Creating character")
                world.actions.add(CreateCharacterAction(NAME))
                WebConnectionState.WaitingCreate
            }
            WebConnectionState.WaitingCreate -> {
                val charId = if (event is NewCharacterCreatedEvent && event.name == NAME) event.charId else null
                if (charId != null) {
                    this.charId = charId
                }
                if (charId == null) WebConnectionState.WaitingCreate else WebConnectionState.Created
            }
            WebConnectionState.Created -> {
                println("Linking character")
                world.actions.add(LinkCharacterAction(charId))
                WebConnectionState.WaitingLink
            }
            WebConnectionState.WaitingLink -> {
                if (!world.characters.containsKey(charId)) {
                    WebConnectionState.WaitingLink
                } else {
                    server.send2Char("", "You are online")
                    doLook(world)
                    WebConnectionState.Playing
                }
            }
            WebConnectionState.Playing -> {
                processEvent(event, world)
                processInput(world)
            }
        }
    }

    private fun send2Char(text: String) {
        server.send2Char("", text)
    }

    private fun processInput(world: World): WebConnectionState {
        val line = server.nextLine("") ?: return WebConnectionState.Playing
        when (line) {
            "l", "look" -> doLook(world) //todo: action?
            "n", "north" -> world.actions.add(WalkAction(charId, Direction.North))
            "s", "south" -> world.actions.add(WalkAction(charId, Direction.South))
            else -> send2Char("Huh?")
        }
        return WebConnectionState.Playing
    }

    private fun processEvent(event: Event, world: World) {
        when {
            event is CharacterLeavesEvent && event.charId == charId -> {
                val place = world.rooms[event.roomId] ?: return
                //todo: leaving
                send2Char(tr.tr(place.name))
            }
            event is CharacterEntersEvent && event.charId == charId -> {
                val place = world.rooms[event.roomId] ?: return
                //todo: entering
                send2Char(tr.tr(place.name))
            }
            event is SayEvent -> {
                //todo: check place
                send2Char(event.message)
            }
        }
    }

    private fun doLook(world: World) {
        val char = world.characters[charId] ?: return
        val place = world.rooms[char.roomId] ?: return
        send2Char(tr.tr(place.name))
        place.characters.filter { it != charId }
                .mapNotNull { world.characters[it] }
                .forEach { send2Char("${it.name} is here") }
    }

}