package midgard.instance

import midgard.ActionId
import midgard.Character
import midgard.CharacterId
import midgard.Direction
import midgard.Event
import midgard.EventId
import midgard.EventLoop
import midgard.ExitInfo
import midgard.IdGenerator
import midgard.ObjectId
import midgard.Place
import midgard.PlaceId
import midgard.Random
import midgard.World
import midgard.buildActionHandlers
import midgard.program.greeting.GuardGreetingProgram
import org.koin.dsl.module.applicationContext

//TODO: rework to Kotlin version of atomic number.
abstract class AbstractIntGenerator<T> : IdGenerator<T> {
    private var counter = 0
    fun incCounter() = ++counter
}

class ActionIdGenerator : AbstractIntGenerator<ActionId>() {
    override fun nextId() = ActionId("a-" + incCounter())
}

class EventIdGenerator : AbstractIntGenerator<EventId>() {
    override fun nextId() = EventId("e-" + incCounter())
}

class CharacterIdGenerator : AbstractIntGenerator<CharacterId>() {
    override fun nextId() = CharacterId("c-" + incCounter())
}

class ObjectIdGenerator : AbstractIntGenerator<ObjectId>() {
    override fun nextId() = ObjectId("o-" + incCounter())
}

val instanceModule = applicationContext {
    bean<Random> { JavaRandom() }
    bean {
        initWorld(World(
                places = loadPlaces(),
                characters = loadCharacters(),
                objects = mutableMapOf(),
                offlineCharacters = loadOfflineCharacters(),
                removedCharacters = loadRemovedCharacters(),
                events = loadPendingEvents(),
                random = get(),
                eventIdGenerator = get<EventIdGenerator>(),
                actionIdGenerator = get<ActionIdGenerator>(),
                characterIdGenerator = get<CharacterIdGenerator>(),
                objectIdGenerator = get<ObjectIdGenerator>())
        )
    }
    bean<EventLoop> { EventLoopImpl() }
    bean { buildActionHandlers() }
    bean { instancePrograms() }
    bean { EventIdGenerator() }
    bean { ActionIdGenerator() }
    bean { CharacterIdGenerator() }
    bean { ObjectIdGenerator() }

    factory { ActionId("a-" + get<ActionIdGenerator>().nextId()) }
    factory { EventId("e-" + get<EventIdGenerator>().nextId()) }
    factory { CharacterId("c-" + get<CharacterIdGenerator>().nextId()) }
}

fun loadPendingEvents() = mutableListOf<Event>()
fun loadRemovedCharacters() = mutableMapOf<CharacterId, Character>()
fun loadOfflineCharacters() = mutableMapOf<CharacterId, Character>()
fun loadCharacters() = mutableMapOf<CharacterId, Character>()
fun loadPlaces() = mutableMapOf<PlaceId, Place>()

class JavaRandom : Random

fun instancePrograms() = listOf(GuardGreetingProgram())

private fun initWorld(world: World): World {
    world.places.putAll(generatePlaces())
    val charId = world.characterIdGenerator.nextId()
    val place = world.places.values.first()
    val char = Character(charId, "Guard", place.id)
    world.characters[charId] = char
    place.characters.add(charId)
    char.programData["greeter"] = "yes"
    return world
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

