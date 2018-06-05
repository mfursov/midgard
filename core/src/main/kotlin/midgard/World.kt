package midgard

import midgard.area.model.Character
import midgard.area.model.CharacterId
import midgard.area.model.Place
import midgard.area.model.PlaceId
import midgard.util.RandomGenerator
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class World : KoinComponent {

    val rnd: RandomGenerator by inject()

    /** All active places in the world. */
    val places = mutableMapOf<PlaceId, Place>()

    /** Online characters map. */
    val characters: MutableMap<CharacterId, Character> = mutableMapOf()

    /** Offline characters map. */
    val offlineCharacters: MutableMap<CharacterId, Character> = mutableMapOf()

    /** Removed characters. They live here for some period of time. */
    val removedCharacters: MutableMap<CharacterId, Character> = mutableMapOf()

    /** List of all pending events. */
    val events: MutableList<Event> = mutableListOf()

    var characterIdCounter = 0
}
