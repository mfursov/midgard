package midgard.event

import midgard.Event
import midgard.area.model.CharacterId

class NewCharacterCreatedEvent(val charId: CharacterId, val name: String) : Event()

class CharacterRemovedEvent(val charId: CharacterId) : Event()

class CharacterLinkedEvent(val charId: CharacterId) : Event()

class CharacterUnlinkedEvent(val charId: CharacterId) : Event()
