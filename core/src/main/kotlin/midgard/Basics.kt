package midgard

import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import kotlin.reflect.KClass

interface Random {
}

interface IdGenerator<T> {
    fun nextId(): T
}

data class EventId(val id: String)

open class Event : KoinComponent {
    val id: EventId by inject()
}

data class ActionId(val id: String)

open class Action : KoinComponent {
    val id: ActionId by inject()
}

interface ActionHandler<A : Action> {
    val actionType: KClass<A>
    fun handleAction(action: A, world: World)
}

interface EventLoop {
    fun post(action: Action)
}

data class ProgramId(val id: String)

abstract class Program(val id: ProgramId) {
    abstract fun tick(world: World)
}
