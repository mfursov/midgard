package midgard.event

import midgard.Event
import midgard.EventId
import midgard.EventType
import midgard.area.model.CharacterId

val LinkCharacterEventType = EventType("link");

class LinkCharacterEvent(id: EventId, val charId: CharacterId) : Event(LinkCharacterEventType, id);

val UnlinkCharacterEventType = EventType("unlink");

class UnlinkCharacterEvent(id: EventId, val charId: CharacterId) : Event(UnlinkCharacterEventType, id);
