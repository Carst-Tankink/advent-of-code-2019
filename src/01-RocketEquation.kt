import RocketEquation.calculateFuel
import RocketEquation.calculateFuelDifferential
import RocketEquation.tests
import java.io.File

object RocketEquation {
    fun calculateFuel(mass: Int): Int = (mass / 3) - 2

    fun calculateFuelDifferential(mass: Int): Int {
        val initialFuel = calculateFuel(mass)

        return when {
            initialFuel <= 0 -> 0
            else -> initialFuel + calculateFuelDifferential(initialFuel)
        }
    }


    fun tests() {
        assert(calculateFuel(12) == 2)
        assert(calculateFuel(14) == 2)
        assert(calculateFuel(100756) == 33583)

        assert(calculateFuelDifferential(14) == 2) { "Mismatch on 14" }
        assert(calculateFuelDifferential(1969) == 966) { "Mismatch on 1969" }
        assert(calculateFuelDifferential(100756) == 50346) { "Mismatch on 100756" }

    }
}


fun main() {
    tests()

    val inputs = File("resources/01-input")
        .readLines()
        .map(String::toInt)
    val result: Long = inputs
        .map { module -> calculateFuel(module).toLong() }
        .sum()

    println("Result: $result")

    val result2: Long = inputs
        .map { module -> calculateFuelDifferential(module).toLong() }
        .sum()

    println("Result: $result2")
}