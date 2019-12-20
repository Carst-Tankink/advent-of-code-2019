import computer.Machine
import computer.State
import util.Location

sealed class ObjectType(val id: Int) {
    object EMPTY : ObjectType(0) {
        override fun toString(): String = " "
    }

    object WALL : ObjectType(1) {
        override fun toString(): String = "#"
    }

    object BLOCK : ObjectType(2) {
        override fun toString(): String = "="
    }

    object PADDLE : ObjectType(3) {
        override fun toString() = "-"
    }

    object BALL : ObjectType(4) {
        override fun toString(): String {
            return "*"
        }
    }

    data class SCORE(val score: Int) : ObjectType(5)

    companion object {
        fun fromId(id: Int): ObjectType {
            return when (id) {
                0 -> EMPTY
                1 -> WALL
                2 -> BLOCK
                3 -> PADDLE
                4 -> BALL
                else -> throw IllegalArgumentException("Unexpected $id")
            }
        }
    }
}

fun runMachine(computer: Machine, auto: Boolean): Map<Location, ObjectType> {
    tailrec fun rec(computer: Machine, screen: Map<Location, ObjectType>): Map<Location, ObjectType> {
        return when (computer.state) {
            State.Halt -> screen
            State.Running -> rec(computer.run(), screen)
            State.Input -> {
                drawScreen(screen)
                val input = if (auto) {
                    val ballPos = screen.entries.find { it.value == ObjectType.BALL }?.key?.x!!
                    val paddlePos = screen.entries.find { it.value == ObjectType.PADDLE }?.key?.x!!
                    when {
                        (paddlePos < ballPos) -> 1L
                        (paddlePos > ballPos) -> -1L
                        else -> 0L
                    }
                } else {
                    println(">")
                    when (readLine()!!) {
                        "a" -> -1L
                        "d" -> 1L
                        else -> 0L
                    }
                }

                rec(computer.input(input), screen)
            }
            State.Output -> {
                val xPos = computer.output.toInt()
                val nextComputer = computer.cont()
                val yPos = nextComputer.output.toInt()
                val location = Location(xPos, yPos)
                val idComputer = nextComputer.cont()

                val nextOutput = idComputer.output.toInt()

                val gameObject = if (location == Location(-1, 0)) ObjectType.SCORE(nextOutput)
                    else ObjectType.fromId(nextOutput)

                rec(idComputer.cont(), screen + Pair(location, gameObject))
            }
        }
    }

    return rec(computer, emptyMap())
}

fun main() {
    val code = Machine.parseProgram("resources/13-input")
    val computer = Machine(code)

    val screen: Map<Location, ObjectType> = runMachine(computer, false)

    println("Blocks on screen ${screen.count { it.value == ObjectType.BLOCK }}")

    val freePlay = code.mapIndexed { index, l -> if (index == 0) 2L else l }

    val newComputer = Machine(freePlay)

    runUntilWin(newComputer, true)

}

fun runUntilWin(newComputer: Machine, auto: Boolean) {
    val screen = runMachine(newComputer, auto)
    if (screen.count { it.value == ObjectType.BLOCK } == 0) {
        drawScreen(screen)
        println(":-)")
    } else {
        println(":-(")
        runUntilWin(newComputer, auto)
    }
}

fun drawScreen(screen: Map<Location, ObjectType>) {
    val scoreLocation = Location(-1, 0)
    val score = screen[scoreLocation] ?: 0
    println("Score: $score")


    val lines: Map<Int, List<ObjectType>> = screen.entries
        .filterNot { it.key == scoreLocation }
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
        println()
    }
}
