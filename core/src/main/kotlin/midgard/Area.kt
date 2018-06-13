package midgard

enum class Direction {
    North, East, South, West, Up, Down;

    fun reverse() = when (this) {
        North -> South
        East -> West
        South -> North
        West -> East
        Up -> Down
        Down -> Up
    }
}


interface Id {
    val id: String
}

data class RoomId(override val id: String) : Id

data class ObjId(override val id: String) : Id

data class CharacterId(override val id: String) : Id

data class Room(
        val id: RoomId,
        val name: String,
        val objects: MutableSet<ObjId>,
        val characters: MutableSet<CharacterId>,
        val exits: MutableMap<Direction, ExitInfo>
)

data class ExitInfo(
        val to: RoomId
)

data class Obj(
        val id: ObjId,
        var containerId: ObjId?,
        var roomId: RoomId?,
        var container: Container
)

data class Container(
        val objects: MutableSet<ObjId> = mutableSetOf()
)

data class Character(
        val id: CharacterId,
        val name: String,
        var roomId: RoomId,

        //todo:
        val programData: MutableMap<String, String> = mutableMapOf()
)
