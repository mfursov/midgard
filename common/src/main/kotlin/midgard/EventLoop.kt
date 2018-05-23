package midgard

import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

data class ActionType(val type: String)
data class ActionId(val id: String)
open class Action(val type: ActionType, val id: ActionId)

data class EventType(val type: String)
data class EventId(val id: String)
open class Event(val type: EventType, val id: EventId)

interface EventListener<in E : Event> {
    fun onEvent(e: E)
}

interface EventLoop {
    fun postAction(action: Action)
    fun addEventListener(eventType: EventType, eventListener: EventListener<Event>)
    fun removeEventListener(eventType: EventType, eventListener: EventListener<Event>)
}

interface ActionHandler<in A : Action> {
    val type: ActionType
    fun handleAction(action: A, world: World)
}

class EventLoopImpl : EventLoop, KoinComponent {
    val world by inject<World>()

    val actionHandlers: Map<ActionType, ActionHandler<Action>> by inject("actionHandlers")
    val eventListeners: MutableMap<EventType, MutableList<EventListener<Event>>> = mutableMapOf()

    override fun postAction(action: Action) {
        val actionHandler = actionHandlers[action.type] ?: throw RuntimeException("Unsupported action type: " + action.type)
        actionHandler.handleAction(action, world)
        world.events.forEach { event -> eventListeners[event.type]?.forEach { it.onEvent(event) } }
        world.events.clear()
    }

    override fun addEventListener(eventType: EventType, eventListener: EventListener<Event>) {
        eventListeners.getOrPut(eventType, { mutableListOf() }).add(eventListener)
    }

    override fun removeEventListener(eventType: EventType, eventListener: EventListener<Event>) {
        eventListeners[eventType]?.remove(eventListener)
    }
}

//TODO: rework to Kotlin version of atomic number.
private var eventIdCounter = 0

fun nextEid(): EventId {
    return EventId("" + (++eventIdCounter))
}
