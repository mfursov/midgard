package midgard

import midgard.area.model.Area
import midgard.area.model.AreaId

class Context {
    val areas: Map<AreaId, Area> = mutableMapOf()
    val server: String = "todo"
}