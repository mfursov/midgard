package midgard.webservice

import midgard.instance.instanceModule
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext
import javax.websocket.Endpoint
import javax.websocket.server.ServerApplicationConfig
import javax.websocket.server.ServerEndpointConfig

@Suppress("unused")
class MidgardWsConfig : ServerApplicationConfig, KoinComponent {

    init {
        StandAloneContext.startKoin(listOf(instanceModule, wsServerModule))
        Thread { WsServer.start() }.start()
    }

    override fun getEndpointConfigs(endpointClasses: Set<Class<out Endpoint>>) =
            setOf<ServerEndpointConfig>(ServerEndpointConfig.Builder.create(MidgardWsEndpoint::class.java, "/websocket").build())

    override fun getAnnotatedEndpointClasses(scanned: Set<Class<*>>): Set<Class<*>> = setOf()
}
