package midgard.instance

import midgard.*
import midgard.db.JsonFormat
import midgard.db.LocalStore
import midgard.db.MPropsTranslator
import midgard.db.Translator
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

class ObjectIdGenerator : AbstractIntGenerator<ObjId>() {
    override fun nextId() = ObjId("o-" + incCounter())
}

val store = LocalStore(JsonFormat())

val instanceModule = applicationContext {
    bean<Random> { JavaRandom() }
    bean {
        initWorld(World(
                rooms = loadPlaces(),
                characters = loadCharacters(),
                objects = mutableMapOf(),
                offlineCharacters = loadOfflineCharacters(),
                removedCharacters = loadRemovedCharacters(),
                events = loadPendingEvents(),
                actions = loadPendingActions(),
                random = get(),
                eventIdGenerator = get<EventIdGenerator>(),
                actionIdGenerator = get<ActionIdGenerator>(),
                characterIdGenerator = get<CharacterIdGenerator>(),
                objectIdGenerator = get<ObjectIdGenerator>(),
                tick = 0L)
        )
    }
    bean<Translator> { MPropsTranslator() }
    bean { EventLoop() }
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
fun loadPendingActions() = mutableListOf<Action>()
fun loadRemovedCharacters() = mutableMapOf<CharacterId, Character>()
fun loadOfflineCharacters() = mutableMapOf<CharacterId, Character>()
fun loadCharacters() = mutableMapOf<CharacterId, Character>()


fun loadPlaces() = store.readRooms().associateBy { it.id }.toMutableMap()

class JavaRandom : Random

fun instancePrograms() = listOf(GuardGreetingProgram())

private fun initWorld(world: World): World {
    val charId = world.characterIdGenerator.nextId()
    val place = world.rooms.values.first()
    val char = Character(charId, "Guard", place.id)
    world.characters[charId] = char
    place.characters.add(charId)
    char.programData["greeter"] = "yes"
    return world
}

