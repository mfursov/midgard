package midgard.event

import midgard.CharacterId
import midgard.Event
import midgard.RoomId

class SayEvent(val charId: CharacterId, val roomId: RoomId, val message: String) : Event()