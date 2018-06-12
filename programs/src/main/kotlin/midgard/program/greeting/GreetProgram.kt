package midgard.program.greeting

import midgard.*
import midgard.event.CharacterEntersEvent

class GuardGreetingProgram : Program(ProgramId("guard-greeting-program"), 10_000) {
    override fun onEvent(event: Event, world: World) {
        if (event !is CharacterEntersEvent) return
        val target = world.characters[event.charId] ?: return
        val place = world.places[event.placeId] ?: return
        place.characters.filter { it != target.id && hasGreetingsProgram(it, world) }.forEach {
            //todo: do say
            val char = world.characters[it] ?: return
            println("${char.name} : Hi ${target.name}")
        }
    }

    private fun hasGreetingsProgram(charId: CharacterId, world: World): Boolean {
        val char = world.characters[charId] ?: return false
        return char.programData.containsKey("greeter")
    }

}