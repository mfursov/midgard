import kotlinx.serialization.json.JSON
import midgard.area.model.Area
import midgard.area.model.AreaId
import org.junit.Test
import kotlin.test.assertEquals

class AreaSerializerTest {

    @Test
    fun serialize() {
        val origArea = Area(AreaId("id1"), "Name", mutableSetOf())
        val serializedArea = JSON.stringify(origArea)
        val restoredArea = JSON.parse<Area>(serializedArea)
        assertEquals(origArea, restoredArea)
    }
}