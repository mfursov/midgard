package midgard.area.generator

import midgard.area.model.Area


abstract class AreaGenerator {
    abstract fun generate(): Area;
}

