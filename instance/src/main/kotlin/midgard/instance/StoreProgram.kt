package midgard.instance

import midgard.Event
import midgard.Program
import midgard.ProgramId
import midgard.World
import midgard.db.Store
import midgard.event.TickEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class StoreProgram : Program(ProgramId("store"), Int.MAX_VALUE), KoinComponent {

    val store: Store by inject()

    override fun onEvent(event: Event, world: World) {
        if (event !is TickEvent || event.tick % 100 != 0L) {
            return
        }
        saveWorld(world)
    }

    fun saveWorld(world: World) {
        println("Saving...")
        store.saveRooms(world.rooms.values)
        println("Saved.")
    }

}