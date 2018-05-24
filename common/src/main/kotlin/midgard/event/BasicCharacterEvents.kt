package midgard.event

import midgard.Event
import midgard.EventType
import midgard.area.model.CharacterId


class NewCharacterCreatedEvent(val charId: CharacterId) : Event(TYPE) {
    companion object {
        val TYPE = EventType("character-created")
    }
}

class CharacterRemovedEvent(val charId: CharacterId) : Event(TYPE) {
    companion object {
        val TYPE = EventType("character-removed")
    }
}


class CharacterLinkedEvent(val charId: CharacterId) : Event(TYPE) {
    companion object {
        val TYPE = EventType("character-linked")
    }
}

class CharacterUnlinkedEvent(val charId: CharacterId) : Event(TYPE) {
    companion object {
        val TYPE = EventType("character-unlinked")
    }
}
