import util.Direction
import util.Direction.*
import util.Location
import java.io.File
import kotlin.math.abs

data class Stretch(val direction: Direction, val start: Location, val end: Location, val distanceOnWire: Int) {
    fun flip(): Stretch {
        return Stretch(
            direction,
            start = end,
            end = start,
            distanceOnWire = distanceOnWire
        )
    }
}

data class Wire(val stretches: List<Stretch>) {

    companion object {
        fun parse(from: String): Wire =
            Wire(
                stretches = from.split(",")
                    .fold(emptyList()) { stretches, descriptor ->
                        val (start, currentDistance) = if (stretches.isEmpty()) {
                            Pair(Location(0, 0), 0)
                        } else {
                            val previous = stretches.last()
                            Pair(previous.end, previous.distanceOnWire)
                        }

                        val direction = Direction.parse(descriptor[0])
                        val distance = descriptor.substring(1).toInt()

                        val endPos = when (direction) {
                            UP -> Location(start.x, start.y + distance)
                            DOWN -> Location(start.x, start.y - distance)
                            RIGHT -> Location(start.x + distance, start.y)
                            LEFT -> Location(start.x - distance, start.y)
                        }

                        stretches + Stretch(direction, start, endPos, currentDistance + distance)
                    }
            )
    }
}

fun main() {
    val isHorizontal: (Stretch) -> Boolean = { stretch -> stretch.direction == LEFT || stretch.direction == RIGHT }
    val inputs: List<Pair<List<Stretch>, List<Stretch>>> = File("resources/03-input")
        .readLines()
        .map { x -> Wire.parse(x) }
        .map { wire -> wire.stretches.partition(isHorizontal) }

    val intersections1 = findIntersections(inputs[0].first, inputs[1].second)
    val intersections2 = findIntersections(inputs[1].first, inputs[0].second)

    val intersections = intersections1 + intersections2
    val manahattanDistances = intersections
        .map { l -> abs(l.first.x) + abs(l.first.y) }
        .sorted()

    println("Shortest Manhattan distance to intersection: ${manahattanDistances[0]}")

    val wireDistances = intersections
        .map { l -> l.second }
        .sorted()

    println("Shortest wire distance to intersection: ${wireDistances[0]}")

}

fun findIntersections(horizontals: List<Stretch>, verticals: List<Stretch>): List<Pair<Location, Long>> {

    val horizontalsAscending = horizontals.map { stretch ->
        if (stretch.direction == RIGHT) stretch else stretch.flip()
    }
    val verticalsAscending = verticals.map { stretch ->
        if (stretch.direction == UP) stretch else stretch.flip()
    }

    return horizontalsAscending.flatMap { h ->
        verticalsAscending
            .filter { h.start.x <= it.start.x && it.start.x <= h.end.x }
            .filter { it.start.y <= h.start.y && h.start.y <= it.end.y }
            .map { v -> Pair(Location(v.start.x, h.start.y), calculateCombinedWireDistance(h, v)) }
            .filter { l -> l.first != Location(0, 0) }
    }
}

fun calculateCombinedWireDistance(horizontal: Stretch, vertical: Stretch): Long {
    // Revert flips used in finding intersection
    val normalizedHorizontal =
        if (horizontal.direction == RIGHT) horizontal else horizontal.flip()
    val normalizedVertical = if (vertical.direction == UP) vertical else vertical.flip()

    val horizontalDistance = normalizedHorizontal.distanceOnWire - if (normalizedHorizontal.direction == RIGHT) {
        (normalizedHorizontal.end.x - vertical.start.x)
    } else {
        (vertical.start.x - normalizedHorizontal.end.x)
    }

    val verticalDistance = normalizedVertical.distanceOnWire - if (normalizedVertical.direction == UP) {
        (normalizedVertical.end.y - horizontal.start.y)
    } else {
        (horizontal.start.y - normalizedVertical.end.y)
    }

    return horizontalDistance + verticalDistance
}
