package midgard.instance

import midgard.Action
import midgard.ActionHandler
import midgard.Program
import midgard.World
import midgard.event.TickEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import kotlin.reflect.KClass

class EventLoop(val millisPerTick: Long) : KoinComponent {

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
            val thread = Thread {
                while (mainThread != null) {
                    tick(world)
                    Thread.sleep(millisPerTick)
                }
            }
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
            try {
                actionHandler.handleAction(it, world)
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    private fun processPendingEvents() {
        while (!world.events.isEmpty()) {
            val event = world.events.removeAt(0)
            programs.forEach {
                try {
                    it.onEvent(event, world)
                } catch (e: Exception) {
                    e.printStackTrace()//todo:
                }
            }
        }
    }
}
