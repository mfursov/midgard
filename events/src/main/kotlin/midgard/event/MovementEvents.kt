package midgard.event

import midgard.Event
import midgard.CharacterId
import midgard.Direction
import midgard.PlaceId


class CharacterLeavesEvent(val charId: CharacterId, val placeId: PlaceId, val direction: Direction) : Event()

class CharacterEntersEvent(val charId: CharacterId, val placeId: PlaceId, val originalDirection: Direction) : Event()
