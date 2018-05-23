package midgard

import org.koin.dsl.module.applicationContext


val appContext = applicationContext {
    bean { World() }
    bean { EventLoopImpl() as EventLoop }
    bean("actionHandlers") { buildActionHandlers() }
}

