package midgard.instance

import midgard.Action
import midgard.ActionHandler
import midgard.Program
import midgard.World
import midgard.event.TickEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import kotlin.reflect.KClass

class EventLoop : KoinComponent {

    private var mainThread: Thread? = null
    private val world by inject<World>()
    private val actionHandlers: Map<KClass<Action>, ActionHandler<Action>> by inject()
    private val programs: List<Program> by inject()

    fun start() {
        println("Started")
        synchronized(this) {
            if (mainThread != null) {
                throw IllegalStateException("Already started")
            }
            val thread = Thread({
                while (mainThread != null) {
                    tick(world)
                    Thread.sleep(100)
                }
            })
            mainThread = thread
            thread.start()
        }
    }

    fun stop() {
        println("Stopping...")
        synchronized(this) {
            if (mainThread == null) {
                throw IllegalStateException("Already stopped")
            }
            mainThread = null
        }
    }

    private fun tick(world: World) {
        processPendingActions()

        world.tick++
        world.events.add(TickEvent(world.tick))

        processPendingEvents()
    }

    //todo: to be called from a dedicated thread
    private fun processPendingActions() {
        val actions = ArrayList(world.actions)
        world.actions.clear()
        actions.forEach {
            val actionType = it::class
            val actionHandler = actionHandlers[actionType] ?: throw RuntimeException("Action has no handler: $actionType")
            actionHandler.handleAction(it, world)
        }
    }

    private fun processPendingEvents() {
        while (!world.events.isEmpty()) {
            val event = world.events.removeAt(0)
            programs.forEach { it.onEvent(event, world) }
        }
    }
}
