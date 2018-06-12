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


data class AreaId(val id: String)

interface Id {
    val id: String
}

data class RoomId(override val id: String) : Id

data class ObjId(val id: String)

data class CharacterId(val id: String)

data class Area(
        val id: AreaId,
        val name: String,
        val places: MutableSet<RoomId>
)

data class Room(
        val id: RoomId,
        val name: String,
        val objects: MutableSet<ObjId> = mutableSetOf(),
        val characters: MutableSet<CharacterId> = mutableSetOf(),
        val exits: MutableMap<Direction, ExitInfo> = mutableMapOf()
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
