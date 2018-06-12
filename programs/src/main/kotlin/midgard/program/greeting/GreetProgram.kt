package midgard.program.greeting

import midgard.CharacterId
import midgard.Event
import midgard.Program
import midgard.ProgramId
import midgard.World
import midgard.action.SayAction
import midgard.event.CharacterEntersEvent

class GuardGreetingProgram : Program(ProgramId("guard-greeting-program"), 10_000) {

    override fun onEvent(event: Event, world: World) {
        if (event !is CharacterEntersEvent) return
        val target = world.characters[event.charId] ?: return
        val place = world.rooms[event.roomId] ?: return
        place.characters.filter { it != target.id && hasGreetingsProgram(it, world) }.forEach {
            val char = world.characters[it] ?: return
            //todo: translate?
            world.actions.add(SayAction(char.id, "${char.name} : Hi ${target.name}"))
        }
    }

    private fun hasGreetingsProgram(charId: CharacterId, world: World): Boolean {
        val char = world.characters[charId] ?: return false
        return char.programData.containsKey("greeter")
    }

}