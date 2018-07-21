package midgard.webservice

import midgard.Event
import midgard.Program
import midgard.ProgramId
import midgard.World
import midgard.instance.EventLoop
import midgard.instance.instancePrograms
import org.koin.dsl.module.applicationContext
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.Session

fun wsServerPrograms() = listOf(WsInterfaceProgram())

val wsServerModule = applicationContext {
    bean { instancePrograms().union(wsServerPrograms()).sortedBy { it.order }.toList() }
    bean<WsInterface> { WsServer }
}

object WsServer : KoinComponent, WsInterface {
    override fun onClose(session: Session) {
        userSessions.remove(session);
    }

    override fun onOpen(session: Session) {
        userSessions.add(session)
    }

    override fun syncState(world: World) {
        broadcast(world.tick)
    }

    override fun nextMessage(): WsMessage {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val userSessions = Collections.newSetFromMap(ConcurrentHashMap<Session, Boolean>())
    fun broadcast(time: Long) {
        for (session in userSessions) {
            session.asyncRemote.sendText(time.toString())
        }
    }

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
    fun nextMessage(): WsMessage
    fun syncState(world: World)
    fun onOpen(session: Session)
    fun onClose(session: Session)
}

interface WsMessage

class WsInterfaceProgram : Program(ProgramId("ws-interface"), 1000), KoinComponent {
    //    private val tr by inject<Translator>()
    private val wsInterface by inject<WsInterface>()

    override fun onEvent(event: Event, world: World) {
        wsInterface.syncState(world)
    }
}