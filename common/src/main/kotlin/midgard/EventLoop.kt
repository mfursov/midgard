package midgard

import midgard.area.model.CharacterId
import midgard.event.CharacterEntersEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.lang.Thread.sleep
import kotlin.reflect.KClass

data class ActionId(val id: String)

open class Action {
    lateinit var id: ActionId
}

data class EventId(val id: String)
open class Event(val id: EventId = nextEventId())

interface EventLoop {
    fun start()
    fun stop()

    fun post(action: Action)
    fun add(program: Program)
}

interface ActionHandler<A : Action> {
    val actionType: KClass<A>
    fun handleAction(action: A, world: World)
}

//todo: add id?
interface Program {
    fun tick(world: World)
}

class EventLoopImpl : EventLoop, KoinComponent {

    var mainThread: Thread? = null
    val world by inject<World>()
    val actionHandlers: Map<KClass<Action>, ActionHandler<Action>> by inject("actionHandlers")
    val programs: MutableList<Program> = loadPrograms()

    val pendingActions = mutableListOf<Action>()

    override fun post(action: Action) {
        action.id = nextActionId()
        pendingActions.add(action)
    }

    override fun add(program: Program) {
        programs.add(program)
    }

    override fun start() {
        synchronized(this) {
            if (mainThread != null) {
                throw IllegalStateException("Already started")
            }
            val thread = Thread({
                while (mainThread != null) {
                    tick(world)
                    sleep(10)
                }
            })
            mainThread = thread
            thread.start()
        }
    }

    override fun stop() {
        synchronized(this) {
            if (mainThread == null) {
                throw IllegalStateException("Already stopped")
            }
            mainThread = null
        }
    }

    private fun tick(world: World) {
        //todo: log
        processPendingActions()
        runPrograms()
        world.events.clear()
    }

    //todo: to be called from a dedicated thread
    private fun processPendingActions() {
        pendingActions.forEach {
            val actionType = it::class
            val actionHandler = actionHandlers[actionType]
                    ?: throw RuntimeException("Action has no handler: $actionType")
            actionHandler.handleAction(it, world)
        }
        pendingActions.clear()
    }

    private fun runPrograms() {
        programs.forEach { it.tick(world) }
    }

    private fun loadPrograms(): MutableList<Program> {
        return mutableListOf(GuardGreetingsProgram())
    }
}

//TODO: rework to Kotlin version of atomic number.
private var actionIdCounter = 0

fun nextActionId(): ActionId {
    return ActionId("a-" + (++actionIdCounter))
}

//TODO: rework to Kotlin version of atomic number.
private var eventIdCounter = 0

fun nextEventId(): EventId {
    return EventId("e-" + (++eventIdCounter))
}

class GuardGreetingsProgram : Program {
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