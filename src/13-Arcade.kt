import computer.Machine
import computer.State
import util.Location

enum class ObjectType(val id: Int) {
    EMPTY(0),
    WALL(1),
    BLOCK(2),
    PADDLE(3),
    BALL(4);

    companion object {
        fun fromId(id: Int) = ObjectType.values().find { it.id == id }!!
    }
}

data class GameObject(val position: Location, val type: ObjectType)

fun runMachine(computer: Machine): List<GameObject> {
    tailrec fun rec(computer: Machine, acc: List<GameObject>): List<GameObject> {
        return when (computer.state) {
            State.Halt -> acc
            State.Running -> rec(computer.run(), acc)
            State.Input -> throw Exception("Not expecting input")
            State.Output -> {
                val xPos = computer.output.toInt()
                val nextComputer = computer.cont()
                val yPos = nextComputer.output.toInt()
                val location = Location(xPos, yPos)

                val idComputer = nextComputer.cont()
                val type = ObjectType.fromId(idComputer.output.toInt())
                rec(idComputer.cont(), acc + GameObject(location, type))
            }
        }
    }

    return rec(computer, emptyList())
}

fun main() {
    val code = Machine.parseProgram("resources/13-input")
    val computer = Machine(code)

    val screen: List<GameObject> = runMachine(computer)


    println("Blocks on screen ${screen.count { it.type == ObjectType.BLOCK}}")
}
