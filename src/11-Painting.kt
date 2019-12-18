import computer.Machine
import util.Direction
import util.Location

data class Robot(
    val program: List<Long>,
    val location: Location = Location(0, 0),
    val direction: Direction = Direction.UP
)

fun main() {
    val program = Machine.parseProgram("resources/11-input")
    val robot = Robot(program)
}