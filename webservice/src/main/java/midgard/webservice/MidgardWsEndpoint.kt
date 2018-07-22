package midgard.webservice

import midgard.json.JSONObject
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

        session.addMessageHandler(String::class.java) { message ->
            println("Message from the client: $message, session: $session")
            this@MidgardWsEndpoint.wsInterface.addIncomingMessage(session, JSONObject(message))
        }
    }

    override fun onClose(session: Session, closeReason: CloseReason) {
        println("Close Connection: $session")
        wsInterface.onClose(session)
    }


    override fun onError(session: Session, e: Throwable) {
        e.printStackTrace()
    }
}
