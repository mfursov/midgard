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

data class PlaceId(override val id: String) : Id

data class ObjId(val id: String)

data class CharacterId(val id: String)

data class Area(
        val id: AreaId,
        val name: String,
        val places: MutableSet<PlaceId>
)

data class Place(
        val id: PlaceId,
        val name: String,
        val objects: MutableSet<ObjId> = mutableSetOf(),
        val characters: MutableSet<CharacterId> = mutableSetOf(),
        val exits: MutableMap<Direction, ExitInfo> = mutableMapOf()
)

data class ExitInfo(
        val toPlaceId: PlaceId
)

data class Obj(
        val id: ObjId,
        var containerId: ObjId?,
        var placeId: PlaceId?,
        var container: Container
)

data class Container(
        val objects: MutableSet<ObjId> = mutableSetOf()
)

data class Character(
        val id: CharacterId,
        val name: String,
        var placeId: PlaceId,

        //todo:
        val programData: MutableMap<String, String> = mutableMapOf()
)
