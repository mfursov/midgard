package midgard.area.generator

import midgard.area.model.Area
import midgard.area.model.AreaId
import midgard.area.model.Direction
import midgard.area.model.Exit
import midgard.area.model.Place
import midgard.area.model.PlaceId

class RoadAreaGenerator : AreaGenerator() {
    enum class TurnsCount {
        /** No turns. */
        None,
        /** Every 4th step has a turn. */
        Low,
        /** Every 3rd step has a turn. */
        Average,
        /** Every 2rd step has a turn. */
        High
    }

    /** Length of the road:
     *  - Small: 2-4 steps
     *  - Medium: 5-7 steps
     *  - Large: 8-10 steps
     */
    var areaSize: AreaSize = AreaSize.Medium

    var turnsCount: TurnsCount = TurnsCount.Low

    override fun generate(): Area {
        val places = generatePlaces(areaSize)
        linkPlaces(places, turnsCount)
        return Area(AreaId("a-1"), places)
    }

    private fun generatePlaces(areaSize: AreaSize) = (1..(
            rnd.nextInt(3) + when (areaSize) {
                AreaSize.Small -> 2
                AreaSize.Medium -> 5
                AreaSize.Large -> 8
            }))
            .map { Place(PlaceId("p-$it")) }

    private fun linkPlaces(places: List<Place>, turnsCount: TurnsCount) {
        var prevDir: Direction? = null
        places.windowed(2).forEach {
            val p1 = it[0]
            val p2 = it[1]
            val dir: Direction = generateNewDirection(p1.exits.keys, prevDir, turnsCount)
            p1.exits[dir] = Exit(p1, p2)
            p2.exits[dir.reverse()] = Exit(p2, p1)
            prevDir = dir
        }
    }

    private fun generateNewDirection(filteredDirections: Set<Direction>, prevDirection: Direction?, turnsCount: TurnsCount): Direction {
        if (prevDirection == null) {
            return selectRandomDirection(filteredDirections)
        }
        if (turnsCount == TurnsCount.None) {
            return prevDirection
        }
        val turnRange = when (turnsCount) {
            TurnsCount.Low -> 4
            TurnsCount.Average -> 3
            else -> 2
        }
        val doTurn = rnd.nextInt(turnRange) == 0
        if (!doTurn) {
            return prevDirection
        }
        return selectRandomDirection(filteredDirections)
    }

    private fun selectRandomDirection(filteredDirections: Set<Direction>): Direction {
        val allowedDirections = mutableListOf(Direction.North, Direction.East, Direction.South, Direction.West)
        allowedDirections.removeAll(filteredDirections)
        if (allowedDirections.isEmpty()) {
            throw IllegalStateException("All directions are filtered!")
        }
        if (allowedDirections.size == 1) {
            return allowedDirections[0]
        }
        return allowedDirections[rnd.nextInt(allowedDirections.size)]
    }
}