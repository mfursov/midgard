package midgard.event

import midgard.Event
import midgard.EventType
import midgard.area.model.CharacterId
import midgard.area.model.Direction


class CharacterEntersEvent(val charId: CharacterId, val originalDirection: Direction) : Event(TYPE) {
    companion object {
        val TYPE = EventType("character-enters")
    }
}

class CharacterLeavesEvents(val charId: CharacterId, val direction: Direction) : Event(TYPE) {
    companion object {
        val TYPE = EventType("character-leaves")
    }
}
