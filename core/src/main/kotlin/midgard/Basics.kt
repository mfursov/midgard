package midgard

import kotlin.reflect.KClass

interface Random

interface IdGenerator<T> {
    fun nextId(): T
}

data class EventId(val id: String)

open class Event {
    val id = EventId("")
}

data class ActionId(val id: String)

open class Action {
    val id = ActionId("")
}

interface ActionHandler<A : Action> {
    val actionType: KClass<A>
    fun handleAction(action: A, world: World)
}

data class ProgramId(val id: String)

abstract class Program(val id: ProgramId, val order: Int) {
    abstract fun onEvent(event: Event, world: World)
}
