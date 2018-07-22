package midgard.webservice

import midgard.*
import midgard.action.WalkAction
import midgard.event.TickEvent
import midgard.instance.EventLoop
import midgard.instance.instancePrograms
import midgard.json.JSONArray
import midgard.json.JSONObject
import org.koin.dsl.module.applicationContext
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.Session
import kotlin.collections.ArrayList

fun wsServerPrograms() = listOf(WsInterfaceProgram())

val wsServerModule = applicationContext {
    bean { instancePrograms().union(wsServerPrograms()).sortedBy { it.order }.toList() }
    bean<WsInterface> { WsServer }
}

object WsServer : KoinComponent, WsInterface {
    private val openSessions = Collections.newSetFromMap(ConcurrentHashMap<Session, Boolean>())
    private val incomingMessages = Collections.synchronizedList(mutableListOf<Pair<Session, JSONObject>>())

    override fun onOpen(session: Session) {
        openSessions.add(session)
    }

    override fun onClose(session: Session) {
        openSessions.remove(session)
    }

    override fun syncState(world: World) {
        openSessions.forEach {
            val serverState = buildServerState(it, world) ?: return
            it.asyncRemote.sendText(serverState.toString())
        }
    }

    override fun addIncomingMessage(session: Session, message: JSONObject) {
        incomingMessages.add(Pair(session, message))
    }

    override fun popIncomingMessages(): List<Pair<Session, JSONObject>> {
        val result = ArrayList(incomingMessages)
        incomingMessages.clear()
        return result
    }


    private fun buildServerState(@Suppress("UNUSED_PARAMETER") it: Session, world: World): JSONObject? {
        val charId = CharacterId("c-1")
        val character = world.characters[charId] ?: return null
        val room = world.rooms[character.roomId] ?: return null

        val serverState = JSONObject()
        serverState["tick"] = world.tick
        serverState["room"] = buildRoomState(room)
        return serverState
    }

    private fun buildRoomState(room: Room) = JSONObject()
            .set("name", room.name)
            .set("exits", buildExitsState(room))

    private fun buildExitsState(room: Room): JSONArray {
        val arr = JSONArray()
        room.exits.forEach {
            arr.add(buildExitState(it.key, it.value))
        }
        return arr
    }

    private fun buildExitState(direction: Direction, exitInfo: ExitInfo) = JSONObject()
            .set("direction", direction.ordinal)
            .set("name", direction.name)//todo:


    private val eventLoop by inject<EventLoop>()

    fun start() {
        eventLoop.start() //todo:
        while (true) {
            Thread.sleep(1000)
        }
        //todo: eventLoop.stop()
    }
}

interface WsInterface {
    fun onOpen(session: Session)
    fun onClose(session: Session)

    fun addIncomingMessage(session: Session, message: JSONObject)
    fun popIncomingMessages(): List<Pair<Session, JSONObject>>

    fun syncState(world: World)

}

class WsInterfaceProgram : Program(ProgramId("ws-interface"), 1000), KoinComponent {

    //    private val tr by inject<Translator>()
    private val wsInterface by inject<WsInterface>()

    override fun onEvent(event: Event, world: World) {
        if (event is TickEvent) {
            wsInterface.syncState(world)
            wsInterface.popIncomingMessages().forEach { handleInput(it.first, it.second, world) }
        }
    }

    private fun handleInput(session: Session, action: JSONObject, world: World) {
        //todo: use session
        val charId = CharacterId("c-1")
        if (action["type"] == "MoveAction") {
            val payload = action.getObject("payload")
            // todo: handle errors
            val directionIdx = payload.getLong("direction").toInt()
            val direction = Direction.values()[directionIdx]
            world.actions.add(WalkAction(charId, direction))
        }
    }
}