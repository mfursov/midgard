package midgard.web

import org.koin.dsl.module.applicationContext


val webAppContext = applicationContext {
    bean { ChatServerImpl() as ChatServer }
}

