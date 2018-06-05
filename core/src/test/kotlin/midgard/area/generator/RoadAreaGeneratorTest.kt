package midgard.area.generator

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RoadAreaGeneratorTest {

    @Test
    fun generateSmallRoad() {
        val gen = RoadAreaGenerator()
        gen.areaSize = AreaGenerator.AreaSize.Small
        val area = gen.generate()
        assertTrue("Expected area size is between 2 and 4", { area.places.size in 2..4 })
    }

    @Test
    fun generateMediumRoad() {
        val gen = RoadAreaGenerator()
        gen.areaSize = AreaGenerator.AreaSize.Medium
        val area = gen.generate()
        assertTrue("Expected area size is between 5 and 7", { area.places.size in 5..7 })
    }

    @Test
    fun generateLargeRoad() {
        val gen = RoadAreaGenerator()
        gen.areaSize = AreaGenerator.AreaSize.Large
        val area = gen.generate()
        assertTrue("Expected area size is between 8 and 10", { area.places.size in 8..10 })
    }

    @Test
    fun generateLargeStraightRoad() {
        val gen = RoadAreaGenerator()
        gen.areaSize = AreaGenerator.AreaSize.Large
        gen.turnsCount = RoadAreaGenerator.TurnsCount.None
        val area = gen.generate()

        assertEquals(1, area.places.first().exits.size)
        assertEquals(1, area.places.last().exits.size)
        area.places.subList(1, area.places.size - 1).forEach { assertEquals(2, it.exits.size) }

        val direction = area.places.first().exits.iterator().next().key
        assertNotNull(area.places.last().exits[direction.reverse()])
        area.places.subList(1, area.places.size - 1).forEach {
            assertNotNull(it.exits[direction])
            assertNotNull(it.exits[direction.reverse()])
        }
    }
}