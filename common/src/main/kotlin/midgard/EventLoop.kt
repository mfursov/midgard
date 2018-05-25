package midgard

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

data class ActionType(val type: String)
data class ActionId(val id: String)

open class Action(val type: ActionType) {
    lateinit var id: ActionId
}

data class EventId(val id: String)
open class Event(val id: EventId = nextEventId())

//todo:
class DummyEvent : Event()

interface EventLoop {
    fun postAction(action: Action): Deferred<Action>
    fun <R> acceptOnce(f: (e: Event) -> R?): Deferred<R>
}

interface ActionHandler<in A : Action> {
    val type: ActionType
    fun handleAction(action: A, world: World)
}

class EventLoopImpl : EventLoop, KoinComponent {
    val world by inject<World>()
    val actionHandlers: Map<ActionType, ActionHandler<Action>> by inject("actionHandlers")

    override fun postAction(action: Action): Deferred<Action> {
        action.id = nextActionId()
        return async(block = {
            val actionHandler = actionHandlers[action.type] ?: throw RuntimeException("Unsupported action type: " + action.type)
            actionHandler.handleAction(action, world)
            action
        })
    }

    override fun <R> acceptOnce(f: (e: Event) -> R?): Deferred<R> {
        val e = DummyEvent()
        return async(block = {
            f(e)!!
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
