import IntCodes.landOnMoon
import IntCodes.runWithNounAndVerb
import IntCodes.tests
import computer.Machine
import computer.State
import java.io.File


object IntCodes {
    fun tests() {
        val input: List<Long> = listOf(
            1, 9, 10, 3,
            2, 3, 11, 0,
            99, 30, 40, 50
        )

        val actual = Machine(input).runSingle().memory
        assert(
            actual == listOf(
                1L, 9, 10, 70,
                2, 3, 11, 0,
                99,
                30, 40, 50
            )
        ) { "Mismatch, actual: $actual" }

        val expected: List<Long> = listOf(
            3500, 9, 10, 70,
            2, 3, 11, 0,
            99,
            30, 40, 50
        )
        testProgram(input, expected)

        testProgram(listOf(1, 0, 0, 0, 99), listOf(2, 0, 0, 0, 99))
        testProgram(listOf(2, 3, 0, 3, 99), listOf(2, 3, 0, 6, 99))
        testProgram(listOf(2, 4, 4, 5, 99), listOf(2, 4, 4, 5, 99, 9801))
        testProgram(listOf(1, 1, 1, 4, 99, 5, 6, 0, 99), listOf(30, 1, 1, 4, 2, 5, 6, 0, 99))
    }

    private fun testProgram(input: List<Long>, expected: List<Long>) {
        val machine = Machine(input).run()
        assert(machine.state == State.Halt)
        assert(
            machine.memory == expected
        ) { "Mismatch. Input: $input, expected $expected actual: ${machine.memory}" }
    }

    fun runWithNounAndVerb(
        inputs: List<Long>,
        noun: Long,
        verb: Long
    ): Long {
        val crashTape = inputs.mapIndexed { i, v ->
            when (i) {
                1 -> noun
                2 -> verb
                else -> v
            }
        }

        return Machine(crashTape).run().memory[0]
    }

    fun landOnMoon(inputs: List<Long>): Pair<Int, Int> {
        val verbs = 0.rangeTo(99)
        val nouns = 0.rangeTo(99)

        for (verb in verbs) for (noun in nouns) {
            val result = runWithNounAndVerb(inputs, noun.toLong(), verb.toLong())
            if (result == 19690720L) return Pair(noun, verb)
        }

        throw Exception("No match found")
    }
}

fun main() {

    tests()

    val inputs = File("resources/02-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toLong)

    val result = runWithNounAndVerb(inputs, 12, 2)
    println("Result $result")

    val result2 = landOnMoon(inputs)

    println("Final result: ${100 * result2.first + result2.second}")
}