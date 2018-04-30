package midgard.area.model


enum class Direction { North, East, South, West, Up, Down }

class PlaceId(val id: String);
class Place(val id: PlaceId, val exits: MutableMap<Direction, Exit>)

class Exit(val from: Place, val to: Place)

class AreaId(val id: String);
class Area(val id: AreaId, val places: List<Place>)

