package midgard

import org.koin.dsl.module.applicationContext


val appContext = applicationContext {
    bean { Midgard() }
    bean { EventLoopImpl() as EventLoop }
    bean("actionHandlers") { buildActionHandlers() }
}

