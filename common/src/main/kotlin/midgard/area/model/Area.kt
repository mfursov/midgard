package midgard.area.model


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

class PlaceId(val id: String);
class Place(val id: PlaceId, val exits: MutableMap<Direction, Exit> = mutableMapOf())

class Exit(val from: Place, val to: Place)

class AreaId(val id: String);
class Area(val id: AreaId, val places: List<Place>)

