package midgard.instance

import midgard.*
import midgard.event.TickEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import kotlin.reflect.KClass

class EventLoopImpl : EventLoop, KoinComponent {

    private var mainThread: Thread? = null
    private val world by inject<World>()
    private val actionHandlers: Map<KClass<Action>, ActionHandler<Action>> by inject()
    private val programs: List<Program> by inject()

    val pendingActions = mutableListOf<Action>()

    override fun post(action: Action) {
        pendingActions.add(action)
    }

    fun start() {
        println("Started")
        synchronized(this) {
            if (mainThread != null) {
                throw IllegalStateException("Already started")
            }
            val thread = Thread({
                while (mainThread != null) {
                    tick(world)
                    Thread.sleep(10)
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
        world.events.add(TickEvent(world.tick))
        runPrograms()
        world.events.clear()
        world.tick++
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
        while (!world.events.isEmpty()) {
            val event = world.events.removeAt(0)
            programs.forEach { it.onEvent(event, world) }
        }
    }
}
