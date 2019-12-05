import Passcodes.tests
import java.io.File

object Passcodes {
    private fun toDigitsRec(accumulator: List<Int>, todo: Int): List<Int> {
        return if (todo <= 0) {
            accumulator
        } else {
            toDigitsRec(listOf(todo % 10) + accumulator, todo / 10)
        }
    }

    private fun toDigits(number: Int): List<Int> {
        return toDigitsRec(emptyList(), number)
    }

    fun isValid(code: Int): Boolean {
        val windows = toDigits(code)
            .windowed(2)
        return windows.any { pair -> pair[0] == pair[1] } && windows.all { pair -> pair[0] <= pair[1] }
    }

    fun tests() {
        assert(toDigits(1234506789) == listOf(1, 2, 3, 4, 5, 0, 6, 7, 8, 9)) { "To digits ${toDigits(1234506789)}" }
        assert(isValid(111111)) { "111111" }
        assert(!isValid(223450)) { "223450" }
        assert(!isValid(123789)) { "123789" }
    }

    fun hasExactPair(code: Int): Boolean {
        return toDigits(code)
            .groupBy { i -> i }
            .any { entry -> entry.value.size == 2 }
    }
}

fun main() {
    tests()

    val input = File("resources/04-input")
        .readLines()
        .flatMap { line -> line.split("-") }
        .map { x -> x.toInt() }

    val from = input[0]
    val to = input[1]

    val validCodes = from.rangeTo(to)
        .filter { code -> Passcodes.isValid(code) }

    println("Valid codes: ${validCodes.count()}")

    val validCodesWithPairRequirement = validCodes
        .filter { code -> Passcodes.hasExactPair(code) }
        .count()


    println("Valid codes with extra requiremet: $validCodesWithPairRequirement")
}