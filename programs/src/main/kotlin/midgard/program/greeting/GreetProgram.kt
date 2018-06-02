package midgard.program.greeting

import midgard.Program
import midgard.ProgramId
import midgard.World
import midgard.area.model.CharacterId
import midgard.event.CharacterEntersEvent

class GuardGreetingProgram : Program(ProgramId("guard-greeting-program")) {
    override fun tick(world: World) {
        world.events.mapNotNull { it as? CharacterEntersEvent }.forEach {
            val target = world.characters[it.charId] ?: return
            val place = world.places[it.placeId] ?: return
            place.characters.filter { it != target.id && hasGreetingsProgram(it, world) }.forEach {
                //todo: do say
                val char = world.characters[it] ?: return
                println("${char.name} : Hi ${target.name}")
            }
        }
    }

    private fun hasGreetingsProgram(charId: CharacterId, world: World): Boolean {
        val char = world.characters[charId] ?: return false
        return char.programData.containsKey("greeter")
    }

}