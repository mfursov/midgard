package midgard

import midgard.area.model.Character
import midgard.area.model.CharacterId
import midgard.area.model.Direction
import midgard.area.model.ExitInfo
import midgard.area.model.Place
import midgard.area.model.PlaceId
import midgard.util.RandomGenerator

class World {
    val rnd = RandomGenerator(1L);

    /** All active places in the world. */
    val places = generatePlaces()

    /** Online characters map. */
    val characters: MutableMap<CharacterId, Character> = mutableMapOf()

    /** Offline characters map. */
    val offlineCharacters: MutableMap<CharacterId, Character> = mutableMapOf()

    /** Removed characters. They live here for some period of time. */
    val removedCharacters: MutableMap<CharacterId, Character> = mutableMapOf()

    /** List of all pending events. */
    val events: MutableList<Event> = mutableListOf()
}

fun generatePlaces(): Map<PlaceId, Place> {
    val places = listOf<Place>()
    val p1 = Place(PlaceId("place-1"), "Place 1")
    val p2 = Place(PlaceId("place-2"), "Place 2")
    p1.exits[Direction.North] = ExitInfo(p1.id)
    p2.exits[Direction.South] = ExitInfo(p2.id)
    return places.associateBy({ it.id })
}

