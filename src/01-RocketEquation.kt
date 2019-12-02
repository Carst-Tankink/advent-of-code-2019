import java.io.File

fun calculateFuel(mass: Int): Long = (mass / 3L) - 2L

fun tests() {
    assert(calculateFuel(12) == 2L)
    assert(calculateFuel(14) == 2L)
    assert(calculateFuel(100756) == 33583L)
}

fun main() {
    tests()

    val result: Long = File("resources/01-input")
        .readLines()
        .map(String::toInt)
        .map { module -> calculateFuel(module) }
        .sum()

    println("Result: $result")

}