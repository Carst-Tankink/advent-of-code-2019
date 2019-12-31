import computer.Machine
import computer.State
import util.Direction
import util.Location

val scaffolds = setOf('#', '^', 'v', '<', '>')
fun main() {
    val program = Machine.parseProgram("resources/17-input")
    val computer = Machine(program)
    val grid = getGrid(computer)

    val intersections: List<Location> = calculateIntersections(grid)
    val alignment = intersections
        .map { it.x * it.y }
        .sum()
    println("Alignment: $alignment")

    printGrid(grid)
    val rawSteps: List<Char> = computeSteps(grid)
    println("Raw steps $rawSteps")
}

fun computeSteps(grid: Map<Location, Char>): List<Char> {
    val scaffoldLocations = grid
        .filterValues { it in scaffolds }
        .keys

    fun hasScaffold(position: Location, newFacing: Direction): Boolean {
        return grid[position.move(newFacing, invertY = true)] == '#'
    }

    fun moveSteps(position: Location, facing: Direction): List<Location> {
        tailrec fun rec(acc: List<Location>, currentPosition: Location): List<Location> {
            val nextLocation = currentPosition.move(facing, invertY = true)
            return if (nextLocation !in scaffoldLocations) acc else rec(acc + nextLocation, nextLocation)
        }

        return rec(emptyList(), position)
    }

    fun getMove(position: Location, facing: Direction): Pair<Direction, List<Location>> {
        val turnDirection =
            if (hasScaffold(position, facing.rotate(Direction.LEFT))) Direction.LEFT else Direction.RIGHT
        val steps = moveSteps(position, facing.rotate(turnDirection))

        return Pair(turnDirection, steps)
    }

    tailrec fun rec(
        acc: List<Char>,
        visitedPositions: Set<Location>,
        position: Location,
        facing: Direction
    ): List<Char> {
        return if (visitedPositions == scaffoldLocations) acc
        else {
            val (direction, positions) = getMove(position, facing)
            val moves = listOf(direction.toChar()) + positions.size.toCharacterValue()

            rec(
                acc = acc + moves,
                visitedPositions = visitedPositions + positions,
                position = positions.last(),
                facing = facing.rotate(direction)
            )
        }
    }

    val start = grid.filterValues { it == '^' }.keys.first()
    return rec(
        emptyList(), setOf(start),
        start, Direction.UP
    )
}

private fun Int.toCharacterValue(): List<Char> {
    return this.toString().toCharArray().toList()
}

private fun Direction.toChar(): Char {
    return when (this) {
        Direction.LEFT -> 'L'
        Direction.RIGHT -> 'R'
        Direction.UP -> 'U'
        Direction.DOWN -> 'D'
    }
}

fun calculateIntersections(grid: Map<Location, Char>): List<Location> {
    return grid
        .filter { isIntersection(it.key, it.value, grid) }
        .keys
        .toList()
}

fun isIntersection(location: Location, entry: Char, grid: Map<Location, Char>): Boolean {
    return entry in scaffolds && location.neighbours().map { grid[it] }.all { it in scaffolds }

}

private fun Location.neighbours(): List<Location> {
    return Direction.values()
        .map { this.move(it, invertY = true) }
        .filter { it.x >= 0 && it.y >= 0 }
}

fun printGrid(grid: Map<Location, Char>) {
    val lines: Map<Long, List<Char>> = grid.entries
        .groupBy { it.key.y }
        .toSortedMap()
        .mapValues { locationLine ->
            locationLine.value
                .sortedBy { entry -> entry.key.x }
                .map { it.value }
        }

    for (line in lines) {
        for (item in line.value) {
            print(item)
        }
    }
}

fun getGrid(computer: Machine): Map<Location, Char> {
    tailrec fun accumulateGrid(
        currentComputer: Machine,
        currentGrid: Map<Location, Char>,
        currentLocation: Location
    ): Map<Location, Char> {
        return when (currentComputer.state) {
            State.Halt -> currentGrid
            State.Running -> accumulateGrid(currentComputer.run(), currentGrid, currentLocation)
            State.Input -> throw Exception("Unexpected input state")
            State.Output -> {
                val nextChar = currentComputer.output.toChar()
                val newGrid = currentGrid + Pair(currentLocation, nextChar)
                val newLocation = if (nextChar == '\n') Location(0, currentLocation.y + 1) else {
                    Location(currentLocation.x + 1, currentLocation.y)
                }
                accumulateGrid(currentComputer.cont(), newGrid, newLocation)
            }
        }
    }
    return accumulateGrid(computer, emptyMap(), Location(0, 0))
}
