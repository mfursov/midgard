package midgard

import kotlinx.serialization.Serializable

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


@Serializable
data class AreaId(val id: String)

@Serializable
data class PlaceId(val id: String)

@Serializable
data class ObjectId(val id: String)

@Serializable
data class CharacterId(val id: String)

@Serializable
data class Area(
        val id: AreaId,
        val name: String,
        val places: MutableSet<PlaceId>
)

@Serializable
data class Place(
        val id: PlaceId,
        val name: String,
        val objects: MutableSet<ObjectId> = mutableSetOf(),
        val characters: MutableSet<CharacterId> = mutableSetOf(),
        val exits: MutableMap<Direction, ExitInfo> = mutableMapOf()
)

@Serializable
data class ExitInfo(
        val toPlaceId: PlaceId
)

@Serializable
data class Object(
        val id: ObjectId,
        var containerId: ObjectId?,
        var placeId: PlaceId?,
        var container: Container
)

@Serializable
data class Container(
        val objects: MutableSet<ObjectId> = mutableSetOf()
)

@Serializable
data class Character(
        val id: CharacterId,
        val name: String,
        var placeId: PlaceId,

        //todo:
        val programData: MutableMap<String, String> = mutableMapOf()
)
