import computer.Machine
import java.io.File

fun main() {
    val program: List<Long> = File("resources/09-input")
        .readLines()
        .flatMap { it.split(",") }
        .map { it.toLong() }

    val machine = Machine(program).run()

    machine.runRecursive(listOf(1)) {println("OUT: $it")}
}