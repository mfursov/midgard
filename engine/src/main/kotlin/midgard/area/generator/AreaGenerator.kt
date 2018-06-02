package midgard.area.generator

import midgard.area.model.Direction
import java.util.Random

class GenPlace(val exits: MutableMap<Direction, GenPlace> = mutableMapOf())
class GenArea(val places: List<GenPlace>)

abstract class AreaGenerator {
    enum class AreaSize {
        Small,
        Medium,
        Large
    }

    protected val rnd = Random(System.currentTimeMillis())

    abstract fun generate(): GenArea;
}

