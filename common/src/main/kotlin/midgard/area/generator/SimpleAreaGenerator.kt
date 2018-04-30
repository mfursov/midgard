package midgard.area.generator

import midgard.area.model.Area
import midgard.area.model.AreaId
import midgard.area.model.Direction
import midgard.area.model.Exit
import midgard.area.model.Place
import midgard.area.model.PlaceId

/**
 * Simples possible area generator. Used for testing only.
 */
class SimpleAreaGenerator : AreaGenerator() {

    companion object {
        val PLACE_1_ID = PlaceId("place-1)")
        val PLACE_2_ID = PlaceId("place-2")
        val AREA_1_ID = AreaId("area-1")
    }

    override fun generate(): Area {
        val p1 = Place(PLACE_1_ID, mutableMapOf());
        val p2 = Place(PLACE_2_ID, mutableMapOf());
        p1.exits[Direction.North] = Exit(p1, p2);
        p2.exits[Direction.South] = Exit(p2, p1);
        return Area(AREA_1_ID, listOf(p1, p2));
    }
}