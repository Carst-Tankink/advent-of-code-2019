import Direction.*
import Wires.tests
import java.io.File
import kotlin.math.abs

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class Location(val x: Int, val y: Int) {
    operator fun plus(other: Location): Location =
        Location(
            this.x + other.x,
            this.y + other.y
        )
}

data class Stretch(val direction: Direction, val start: Location, val end: Location)

data class Wire(val stretches: List<Stretch>) {

    companion object {
        fun parse(from: String): Wire =
            Wire(
                stretches = from.split(",")
                    .map { x ->
                        val distance = x.substring(1).toInt()
                        val direction = when (x[0]) {
                            'U' -> UP
                            'D' -> DOWN
                            'L' -> LEFT
                            'R' -> RIGHT
                            else -> throw Exception("Unexpected input ${x[0]}")
                        }

                        val endPos = when (direction) {
                            UP -> Location(0, distance)
                            DOWN -> Location(0, -1 * distance)
                            RIGHT -> Location(distance, 0)
                            LEFT -> Location(-1 * distance, 0)

                        }

                        Stretch(direction, Location(0, 0), endPos)
                    }
            )
    }
}


object Wires {

    fun tests() {

    }
}

fun main() {
    tests()
    val inputs: List<Wire> = File("resources/03-input")
        .readLines()
        .map { x -> Wire.parse(x) }

    assert(inputs.size == 2)

    val translated = inputs.map { wire ->
        val translatedStretches: List<Stretch> = wire.stretches.fold(emptyList()) { acc, stretch ->
            val previousEnd = if (acc.isEmpty()) Location(0, 0) else acc.last().end
            acc + Stretch(stretch.direction, stretch.start + previousEnd, stretch.end + previousEnd)
        }

        Wire(translatedStretches)
    }

    val wire1 = translated[0]
    println("Wire1: $wire1")
    val wire2 = translated[1]
    println("Wire2: $wire2")

    val isHorizontal: (Stretch) -> Boolean = { stretch -> stretch.direction == LEFT || stretch.direction == RIGHT }
    val wire1Partitions = wire1.stretches.partition(isHorizontal)
    println("partitions: $wire1Partitions")
    val wire2Partitions = wire2.stretches.partition(isHorizontal)

    val intersections: List<Location> = findIntersections(wire1Partitions.first, wire2Partitions.second)
    val intersections2: List<Location> = findIntersections(wire2Partitions.first, wire1Partitions.second)

    println("Instersections: $intersections")
    println("Instersections: $intersections2")
    val shortestDistance = (intersections + intersections2)
        .filter { l -> l != Location(0, 0) }
        .map { l -> abs(l.x) + abs(l.y) }
        .sorted()

    println("Shortest distance to intersection: $shortestDistance")

}

fun findIntersections(horizontals: List<Stretch>, verticals: List<Stretch>): List<Location> {

    val horizontalsAscending = horizontals.map { stretch ->
        if (stretch.start.x < stretch.end.x) stretch else Stretch(
            stretch.direction,
            stretch.end,
            stretch.start
        )
    }
    val verticalsAscending = verticals.map { stretch ->
        if (stretch.start.y < stretch.end.y) stretch else Stretch(
            stretch.direction,
            stretch.end,
            stretch.start
        )
    }

    return horizontalsAscending.flatMap { h ->
        verticalsAscending
            .filter { h.start.x <= it.start.x && it.start.x <= h.end.x }
            .filter { it.start.y <= h.start.y && h.start.y <= it.end.y }
            .map { Location(it.start.x, h.start.y) }
    }
}
