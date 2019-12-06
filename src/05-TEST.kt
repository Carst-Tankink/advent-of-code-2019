import computer.Machine
import java.io.File

fun main() {
    val codes: List<Int> = File("resources/05-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toInt)

    Machine().run(codes)
}