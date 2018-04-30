package midgard.area.model


enum class Direction { North, East, South, West, Up, Down }

class Place(val exits: Map<Direction, Exit>)

class Exit(val from: Place, val to: Place)


