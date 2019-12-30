import computer.Machine
import computer.State
import util.Direction
import util.Location

fun main() {
    val program = Machine.parseProgram("resources/17-input")
    val computer = Machine(program)
    val grid = getGrid(computer)

    val intersections: List<Location> = calculateIntersections(grid)
    val alignment = intersections
        .map { it.x * it.y }
        .sum()
    println("Alignment: $alignment")
}

fun calculateIntersections(grid: Map<Location, Char>): List<Location> {
    return grid
        .filter { isIntersection(it.key, it.value, grid) }
        .keys
        .toList()
}

fun isIntersection(location: Location, entry: Char, grid: Map<Location, Char>): Boolean {
    val scaffolds = setOf('#', '^', 'v', '<', '>')
    return entry in scaffolds && location.neighbours().map { grid[it] }.all { it in scaffolds }

}

private fun Location.neighbours(): List<Location> {
    return Direction.values()
        .map { this.move(it) }
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
