import computer.Machine
import computer.State
import java.io.File

fun main() {
    val codes: List<Long> = File("resources/05-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toLong)

    Machine(codes).runRecursive(listOf(1)) { println("OUT: $it") }
    Machine(codes).runRecursive(listOf(5)) { println("OUT: $it") }
}