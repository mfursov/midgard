package midgard.instance

import midgard.*
import midgard.db.*
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

val dataDir = System.getProperty("midgard.dataDir") ?: throw IllegalStateException("'midgard.dataDir' system property is not set")

val instanceModule = applicationContext {
    bean<Random> { JavaRandom() }
    bean {
        initWorld(World(
                rooms = loadRooms(get()),
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
    bean { EventLoop() }
    bean { buildActionHandlers() }
    bean { instancePrograms() }
    bean { EventIdGenerator() }
    bean { ActionIdGenerator() }
    bean { CharacterIdGenerator() }
    bean { ObjectIdGenerator() }

    bean<Translator> { MPropsTranslator(dataDir) }
    bean<Store> { LocalStore(dataDir, JsonFormat()) }

    factory { ActionId("a-" + get<ActionIdGenerator>().nextId()) }
    factory { EventId("e-" + get<EventIdGenerator>().nextId()) }
    factory { CharacterId("c-" + get<CharacterIdGenerator>().nextId()) }
}

fun loadPendingEvents() = mutableListOf<Event>()
fun loadPendingActions() = mutableListOf<Action>()
fun loadRemovedCharacters() = mutableMapOf<CharacterId, Character>()
fun loadOfflineCharacters() = mutableMapOf<CharacterId, Character>()
fun loadCharacters() = mutableMapOf<CharacterId, Character>()


fun loadRooms(store: Store) = store.loadRooms().associateBy { it.id }.toMutableMap()

class JavaRandom : Random

fun instancePrograms() = listOf(
        GuardGreetingProgram(),
        StoreProgram()
)

private fun initWorld(world: World): World {
    val charId = world.characterIdGenerator.nextId()
    val room = world.rooms.values.first()
    val char = Character(charId, "Guard", room.id)
    world.characters[charId] = char
    room.characters.add(charId)
    char.programData["greeter"] = "yes"
    return world
}
