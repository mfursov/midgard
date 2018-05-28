package midgard.event

import midgard.Event
import midgard.area.model.CharacterId
import midgard.area.model.Direction
import midgard.area.model.PlaceId


class CharacterLeavesEvent(val charId: CharacterId, val placeId: PlaceId, val direction: Direction) : Event()

class CharacterEntersEvent(val charId: CharacterId, val placeId: PlaceId, val originalDirection: Direction) : Event()
