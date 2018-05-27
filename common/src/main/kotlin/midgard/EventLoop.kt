package midgard

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

//todo:
class DummyEvent : Event()

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

interface Program {
    fun tick(world: World)
}

class EventLoopImpl : EventLoop, KoinComponent {

    var mainThread: Thread? = null
    val world by inject<World>()
    val actionHandlers: Map<KClass<Action>, ActionHandler<Action>> by inject("actionHandlers")
    val programs = mutableListOf<Program>()
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
            mainThread = thread;
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

    fun tick(world: World) {
        //todo: log
        processPendingActions()
        runPrograms()
    }

    //todo: to be called from a dedicated thread
    fun processPendingActions() {
        pendingActions.forEach {
            val actionType = it::class
            val actionHandler = actionHandlers[actionType] ?: throw RuntimeException("Action has no handler: $actionType")
            actionHandler.handleAction(it, world)
        }
    }

    fun runPrograms() {
        programs.forEach { it.tick(world) }
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
