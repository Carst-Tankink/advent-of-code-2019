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

fun searchOxygenTank(robot: RepairDroid): Pair<Location, Long> {
    tailrec fun rec(robots: List<RepairDroid>, steps: Long, seenLocations: Set<Location>): Pair<Location, Long> {
        return if (robots.any { it.oxygenPosition != null }) Pair(
            robots.mapNotNull { it.oxygenPosition }.first(),
            steps
        ) else {
            val newRobots: List<RepairDroid> = robots.flatMap { droid -> Direction.values().map { droid.move(it) } }
                .filterNot { newRobot -> newRobot.position == robot.position }
                .filterNot { seenLocations.contains(it.position) }

            rec(newRobots, steps + 1, seenLocations + newRobots.map { robot -> robot.position })
        }
    }

    return rec(listOf(robot), 0, emptySet())
}

fun main() {
    val program = Machine.parseProgram("resources/15-input")

    val (oxygen, steps) = searchOxygenTank(RepairDroid(Machine(program)))
    println("Oxygen is at: $oxygen, taking $steps steps")
}