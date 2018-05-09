package midgard.web

import org.koin.dsl.module.applicationContext

val appContext = applicationContext {
    bean { ChatServerImpl() as ChatServer }
}

