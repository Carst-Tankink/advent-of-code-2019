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
    val rawSteps = computeSteps(grid)
    println("Raw steps ${compressSteps(rawSteps)}")


    val cleaned = runCleanup(program, rawSteps)
    println("Cleaned: $cleaned")
}

fun runCleanup(program: List<Long>, rawSteps: String): Long {
    val computer = Machine(listOf(2L) + program.drop(1))
    val (aDef, bDef, cDef) = extractRoutines(rawSteps)!!
    val mainRoutine = replaceRoutines(rawSteps, aDef, bDef, cDef)

    return executeMovement(computer, mainRoutine, aDef, bDef, cDef)
}

fun executeMovement(computer: Machine, mainRoutine: String, aDef: String, bDef: String, cDef: String): Long {
    tailrec fun rec(state: Machine, toProvide: List<String>): Long {
        return when (state.state) {
            State.Halt -> state.output
            State.Output -> {
                val output = state.output
                print(output.toChar())

                rec(state.cont(), toProvide)
            }
            State.Input -> {
                rec(state.inputAscii(toProvide.first()), toProvide.drop(1))
            }

            State.Running -> rec(state.run(), toProvide)
        }
    }

    return rec(computer, listOf(mainRoutine, aDef, bDef, cDef).map { compressSteps(it) } + "n")
}

fun prefixes(s: String): List<String> {
    tailrec fun rec(acc: List<String>, left: String): List<String> {
        return if (left.isEmpty()) acc else {
            val nextChar = left.first()
            val newAcc = acc + if (acc.isEmpty()) nextChar.toString() else acc.last() + nextChar

            rec(newAcc, left.drop(1))
        }
    }

    return rec(emptyList(), s)
}

fun compressSteps(rawSteps: String): String {
    tailrec fun rec(acc: String, toGo: String): String {
        return if (toGo.isEmpty()) acc else {
            val (toAdd, toDrop) = when (toGo.first()) {
                'R' -> Pair("R", 1)
                'L' -> Pair("L", 1)
                'A' -> Pair("A", 1)
                'B' -> Pair("B", 1)
                'C' -> Pair("C", 1)
                'f' -> {
                    val fs = toGo.takeWhile { it == 'f' }
                    Pair(fs.length.toString(), fs.length)
                }
                else -> throw Exception("Unexpected command: $toGo")
            }


            val newAcc: String = if (acc.isEmpty()) toAdd else "$acc,$toAdd"
            rec(newAcc, toGo.drop(toDrop))
        }
    }

    return rec("", rawSteps)
}

fun extractRoutines(rawSteps: String): Triple<String, String, String>? {
    return prefixes(rawSteps)
        .filter { compressSteps(it).length <= 20 }
        .flatMap { aCandidate ->
            val next = replaceRoutines(rawSteps, aCandidate, "X", "X").trimEnd(',')
                .dropWhile { it == 'A' }.takeWhile { it != 'A' }
            prefixes(next)
                .filter { compressSteps(it).length <= 20 }
                .flatMap { bCandidate ->
                    val replacedB = replaceRoutines(rawSteps, aCandidate, bCandidate, "X").trimEnd(',')
                        .dropWhile { it == 'A' || it == 'B' }
                        .takeWhile { it != 'A' && it != 'B' }
                    prefixes(replacedB)
                        .filter { compressSteps(it).length <= 20 }
                        .map { cCandidate ->
                            Triple(aCandidate, bCandidate, cCandidate)
                        }
                }
        }.find { (a, b, c) ->
            val call = replaceRoutines(rawSteps, a, b, c)
            call.all { it == 'A' || it == 'B' || it == 'C' } && compressSteps(call).length <= 20
        }
}

fun replaceRoutines(definition: String, aRoutine: String, bRoutine: String, cRoutine: String): String {
    return when {
        definition.isEmpty() -> ""
        definition.startsWith(aRoutine) -> "A" + replaceRoutines(
            definition.removePrefix(aRoutine),
            aRoutine,
            bRoutine,
            cRoutine
        )
        definition.startsWith(bRoutine) -> "B" + replaceRoutines(
            definition.removePrefix(bRoutine),
            aRoutine,
            bRoutine,
            cRoutine
        )
        definition.startsWith(cRoutine) -> "C" + replaceRoutines(
            definition.removePrefix(cRoutine),
            aRoutine,
            bRoutine,
            cRoutine
        )
        else -> definition.first() + replaceRoutines(definition.drop(1), aRoutine, bRoutine, cRoutine)
    }
}

fun computeSteps(grid: Map<Location, Char>): String {
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
        acc: String,
        visitedPositions: Set<Location>,
        position: Location,
        facing: Direction
    ): String {
        return if (visitedPositions == scaffoldLocations) acc
        else {
            val (direction, positions) = getMove(position, facing)


            val moves = direction.toChar() + positions.joinToString("") { "f" }
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
        "", setOf(start),
        start, Direction.UP
    )
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
                val newGrid = if (nextChar == '\n') currentGrid else currentGrid + Pair(currentLocation, nextChar)
                val newLocation = if (nextChar == '\n') Location(0, currentLocation.y + 1) else {
                    Location(currentLocation.x + 1, currentLocation.y)
                }
                accumulateGrid(currentComputer.cont(), newGrid, newLocation)
            }
        }
    }
    return accumulateGrid(computer, emptyMap(), Location(0, 0))
}
