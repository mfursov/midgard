package midgard.area.generator

import midgard.Direction

/**
 * Simples possible area generator. Used for testing only.
 */
class SimpleAreaGenerator : AreaGenerator() {

    override fun generate(): GenArea {
        val p1 = GenPlace(mutableMapOf())
        val p2 = GenPlace(mutableMapOf())
        p1.exits[Direction.North] = p2
        p2.exits[Direction.South] = p1
        return GenArea(listOf(p1, p2))
    }
}