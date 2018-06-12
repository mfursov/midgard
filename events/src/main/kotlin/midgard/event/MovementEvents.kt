package midgard.event

import midgard.CharacterId
import midgard.Direction
import midgard.Event
import midgard.RoomId


class CharacterLeavesEvent(val charId: CharacterId, val roomId: RoomId, val direction: Direction) : Event()

class CharacterEntersEvent(val charId: CharacterId, val roomId: RoomId, val originalDirection: Direction) : Event()
