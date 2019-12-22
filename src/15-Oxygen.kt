import computer.Machine
import computer.State
import util.Direction
import util.Location

data class RepairDroid(
    val computer: Machine,
    val position: Location = Location(0, 0),
    val attemptedDirection: Direction = Direction.UP,
    val oxygenPosition: Location? = null
) {
    fun move(dir: Direction): RepairDroid {
        return when (computer.state) {
            State.Halt -> this
            State.Running -> copy(computer = computer.run()).move(dir)
            State.Input -> {
                val dirInput = when (dir) {
                    Direction.UP -> 1L
                    Direction.DOWN -> 2L
                    Direction.LEFT -> 3L
                    Direction.RIGHT -> 4L
                }
                copy(computer = computer.input(dirInput), attemptedDirection = dir).move(dir)
            }
            State.Output -> {
                val newComputer = computer.cont()
                val newPosition = position.move(attemptedDirection)
                when (computer.output) {
                    0L -> copy(computer = newComputer)
                    1L -> copy(computer = newComputer, position = newPosition)
                    2L -> copy(computer = newComputer, position = newPosition, oxygenPosition = newPosition)
                    else -> throw IllegalStateException("Unexpected output state ${computer.output}")
                }
            }

        }
    }
}

fun searchOxygenTank(robot: RepairDroid): Triple<Location, Long, Set<Location>> {
    tailrec fun rec(
        robots: List<RepairDroid>,
        steps: Long,
        oxygenPosition: Location?,
        seenLocations: Set<Location>
    ): Triple<Location, Long, Set<Location>> {
        val newRobots: List<RepairDroid> = robots.flatMap { droid -> Direction.values().map { droid.move(it) } }
            .filterNot { newRobot -> newRobot.position == robot.position }
            .filterNot { seenLocations.contains(it.position) }
        val foundOxygen = oxygenPosition ?: robots.find { it.oxygenPosition != null }?.oxygenPosition
        return if (newRobots.isEmpty()) {
            Triple(
                oxygenPosition!!,
                steps,
                seenLocations
            )
        } else {
            val newSteps = if (foundOxygen != null) steps else steps + 1
            rec(newRobots,
                newSteps,
                foundOxygen,
                seenLocations + newRobots.map { robot -> robot.position })
        }
    }

    return rec(listOf(robot), 0, null, emptySet())
}

fun fillWithOxygen(oxygen: Location, map: Set<Location>): Int {
    tailrec fun rec(done: Set<Location>, minutes: Int): Int {
        return if (done == map) minutes else {
            val newOxygen = done
                .flatMap { location -> Direction.values().map { location.move(it) } }
                .filter { map.contains(it) }
                .toSet()
            rec(done + newOxygen, minutes + 1)
        }
    }

    return rec(setOf(oxygen), 0)
}

fun main() {
    val program = Machine.parseProgram("resources/15-input")

    val (oxygen, steps, map) = searchOxygenTank(RepairDroid(Machine(program)))
    println("Oxygen is at: $oxygen, taking $steps steps")

    val minutes = fillWithOxygen(oxygen, map)
    println("Filled in $minutes minutes")
}
