package midgard.area.generator

import midgard.Direction
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame


internal class SimpleAreaGeneratorTest {

    @Test
    fun generate() {
        val area = SimpleAreaGenerator().generate()
        assertEquals(2, area.places.size)
        assertSame(area.places[0].exits[Direction.North], area.places[1])
        assertSame(area.places[1].exits[Direction.South], area.places[0])
    }
}