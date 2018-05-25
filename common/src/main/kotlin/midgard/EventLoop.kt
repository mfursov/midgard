package midgard

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
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
    fun post(action: Action): Deferred<Action>
    fun <R> acceptOnce(f: (e: Event) -> R?): Deferred<R>
}

interface ActionHandler<in A : Action> {
    fun handleAction(action: A, world: World)
}

class EventLoopImpl : EventLoop, KoinComponent {
    val world by inject<World>()
    val actionHandlers: Map<KClass<Action>, ActionHandler<Action>> by inject("actionHandlers")

    override fun post(action: Action): Deferred<Action> {
        action.id = nextActionId()
        return async(block = {
            val actionType = action::class
            val actionHandler = actionHandlers[actionType] ?: throw RuntimeException("Unsupported action type: $actionType")
            actionHandler.handleAction(action, world)
            action
        })
    }

    override fun <R> acceptOnce(f: (e: Event) -> R?): Deferred<R> {
        val e = DummyEvent()
        return async(block = {
            f(e)!! //todo
        })
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
