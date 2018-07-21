package midgard.webservice

import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import javax.websocket.CloseReason
import javax.websocket.Endpoint
import javax.websocket.EndpointConfig
import javax.websocket.Session

class MidgardWsEndpoint : Endpoint(), KoinComponent {

    private val wsInterface by inject<WsInterface>()

    override fun onOpen(session: Session, config: EndpointConfig) {
        println("Open Connection: $session")
        wsInterface.onOpen(session)
        //session.addMessageHandler(String::class.java) { message -> this@MidgardWsEndpoint.onMessage(message, session) }
    }

//    private fun onMessage(message: String, session: Session) {
//        println("Message from the client: $message, session: $session")
//        session.basicRemote.sendText("Echo from the server : $message")
//    }

    override fun onClose(session: Session, closeReason: CloseReason) {
        println("Close Connection: $session")
        wsInterface.onClose(session)
    }


    override fun onError(session: Session, e: Throwable) {
        //todo:
        e.printStackTrace()
    }
}
