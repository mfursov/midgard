package midgard.area.generator

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame


internal class SimpleAreaGeneratorTest {

    @Test
    fun generate() {
        val area = SimpleAreaGenerator().generate()
        assertEquals(SimpleAreaGenerator.AREA_1_ID, area.id)
        assertEquals(2, area.places.size)
        assertSame(SimpleAreaGenerator.PLACE_1_ID, area.places[0].id)
        assertSame(SimpleAreaGenerator.PLACE_2_ID, area.places[1].id)
    }
}