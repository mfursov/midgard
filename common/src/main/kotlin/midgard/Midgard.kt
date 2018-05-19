package midgard

import midgard.area.model.Character
import midgard.area.model.CharacterId
import midgard.area.model.Direction
import midgard.area.model.ExitInfo
import midgard.area.model.Place
import midgard.area.model.PlaceId

class Midgard {
    val places = generatePlaces()
    val characters: MutableMap<CharacterId, Character> = mutableMapOf()
}

fun generatePlaces(): Map<PlaceId, Place> {
    val places = listOf<Place>()
    val p1 = Place(PlaceId("place-1"), "Place 1")
    val p2 = Place(PlaceId("place-2"), "Place 2")
    p1.exits[Direction.North] = ExitInfo(p1.id)
    p2.exits[Direction.South] = ExitInfo(p2.id)
    return places.associateBy({ it.id })
}

