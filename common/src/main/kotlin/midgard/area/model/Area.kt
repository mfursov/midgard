package midgard.area.model

import kotlinx.serialization.Serializable
import java.util.EnumMap

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
data class ObjId(val id: String)

@Serializable
data class CharId(val id: String)

@Serializable
data class Area(
        val id: AreaId,
        val name: String,
        val places: MutableSet<PlaceId>
)

@Serializable
data class Place(
        val id: AreaId,
        val name: String,
        val objects: MutableSet<ObjId>,
        val characters: MutableSet<CharId>,
        val exits: EnumMap<Direction, ExitInfo>
)

@Serializable
data class ExitInfo(
        val toPlaceId: PlaceId
)

@Serializable
data class Obj(
        val id: ObjId
)

@Serializable
data class Char(
        val id: CharId
)

