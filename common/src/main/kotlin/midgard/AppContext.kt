package midgard

import midgard.area.model.Character
import midgard.area.model.CharacterId
import org.koin.dsl.module.applicationContext


val appContext = applicationContext {
    bean { loadWorld() }
    bean { EventLoopImpl() as EventLoop }
    bean("actionHandlers") { buildActionHandlers() }
}

private fun loadWorld(): World {
    val world = World()
    val charId = CharacterId("${++world.characterIdCounter}")
    val place = world.places.values.first()
    val char = Character(charId, "Guard", place.id)
    world.characters[charId] = char
    place.characters.add(charId)
    char.programData["greeter"] = "yes"
    return world
}

