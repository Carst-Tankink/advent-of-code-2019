import Direction.*
import Wires.tests
import java.io.File

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

data class Stretch(val direction: Direction, val start: Pair<Int, Int>, val end: Pair<Int, Int>)

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
                            UP -> Pair(0, distance)
                            DOWN -> Pair(0, -1 * distance)
                            RIGHT -> Pair(distance, 0)
                            LEFT -> Pair(-1 * distance, 0)

                        }

                        Stretch(direction, Pair(0, 0), endPos)
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
            val previousEnd = if (acc.isEmpty()) Pair(0, 0) else acc.last().end
            acc + Stretch(stretch.direction, stretch.start + previousEnd, stretch.end + previousEnd)
        }

        Wire(translatedStretches)
    }

}

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(
        this.first + other.first,
        this.second + other.second
    )