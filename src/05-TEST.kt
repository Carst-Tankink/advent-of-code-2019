import computer.Machine
import java.io.File

fun main() {
    val codes: List<Long> = Machine.parseProgram("resources/05-input")

    Machine(codes).runRecursive(listOf(1)) { println("OUT: $it") }
    Machine(codes).runRecursive(listOf(5)) { println("OUT: $it") }
}