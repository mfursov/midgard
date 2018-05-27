package midgard.event

import midgard.Event
import midgard.area.model.CharacterId
import midgard.area.model.Direction


class CharacterEntersEvent(val charId: CharacterId, val originalDirection: Direction) : Event()

class CharacterLeavesEvents(val charId: CharacterId, val direction: Direction) : Event()
