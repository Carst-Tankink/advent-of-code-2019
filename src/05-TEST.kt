import computer.Machine
import java.io.File

fun main() {
    val codes: List<Long> = File("resources/05-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toLong)

    Machine().run(codes)
}