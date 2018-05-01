package midgard.area.generator

import midgard.area.model.Area
import java.util.Random

abstract class AreaGenerator {
    enum class AreaSize {
        Small,
        Medium,
        Large
    }

    protected val rnd = Random(System.currentTimeMillis())

    abstract fun generate(): Area;
}

