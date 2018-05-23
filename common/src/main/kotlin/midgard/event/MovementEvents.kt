package midgard.event

import midgard.Event
import midgard.EventId
import midgard.EventType
import midgard.area.model.CharacterId
import midgard.area.model.Direction

val WalkEventType = EventType("walk");

class WalkEvent(id: EventId, val charId: CharacterId, val originalDirection: Direction) : Event(WalkEventType, id);
