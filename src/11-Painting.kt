import computer.Machine
import computer.State
import util.Direction
import util.Location

data class Robot(
    val computer: Machine,
    val location: Location = Location(0, 0),
    val direction: Direction = Direction.UP
) {
    fun stopped(): Boolean = computer.state == State.Halt
    fun step(hull: Map<Location, Int>): Pair<Robot, Map<Location, Int>> {
        return when (computer.state) {
            State.Halt -> Pair(this, hull)
            State.Running -> Pair(copy(computer = computer.run()), hull)
            State.Output -> {
                val color: Int = computer.output.toInt()
                val newHull = hull + Pair(location, color)
                val withRotation = computer.cont()
                if (withRotation.state != State.Output) {
                    throw Exception("Unexpected state: ${withRotation.state}")
                }
                val turn: Direction = when (val rotation: Int = withRotation.output.toInt()) {
                    0 -> Direction.LEFT
                    1 -> Direction.RIGHT
                    else -> throw Exception("Unexpected rotation $rotation")
                }

                val newDirection = direction.rotate(turn)
                val newLocation = location.move(newDirection)
                Pair(copy(computer = withRotation.cont(), location = newLocation, direction = newDirection), newHull)
            }
            State.Input -> {
                val currentColor = hull[location] ?: 0
                Pair(copy(computer = computer.input(currentColor.toLong())), hull)
            }
        }
    }
}

// Will return a map of all painted locations and the painted color
fun paint(robot: Robot, initialHull: Map<Location, Int>): Map<Location, Int> {
    tailrec fun rec(robot: Robot, acc: Map<Location, Int>): Map<Location, Int> {
        return if (robot.stopped()) acc else {
            val (newRobot, newHull) = robot.step(acc)
            rec(newRobot, newHull)
        }
    }

    return rec(robot, initialHull)
}

fun main() {
    val program = Machine.parseProgram("resources/11-input")
    val robot = Robot(Machine(program))

    val hull = paint(robot, emptyMap())

    println("Painted: ${hull.size}")


    val registration = paint(robot, mapOf(Pair(Location(0, 0), 1)))

    val painted = registration
        .filterValues { it == 1 }
        .keys

    val xes = painted.map { it.x }
    val maxX = xes.max() ?: 0
    val minX = xes.min() ?: 0
    val ys = painted.map { it.y }
    val maxY = ys.max() ?: 0
    val minY = ys.min() ?: 0

    for (y in maxY.downTo(minY)) {
        for (x in minX.rangeTo(maxX)) {
            val toPrint = if (painted.contains(Location(x, y))) "#" else " "
            print(toPrint)
        }
        println()
    }
}