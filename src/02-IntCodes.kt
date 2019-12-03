import IntCodes.landOnMoon
import IntCodes.runWithNounAndVerb
import IntCodes.tests
import java.io.File

data class Machine(val position: Int, val tape: List<Int>) {
    val operation: Int = tape[position]
}

object IntCodes {
    private fun iterateOnce(machine: Machine): Machine {
        return when (machine.operation) {
            1 -> doOperation(machine) { x, y -> x + y }
            2 -> doOperation(machine) { x, y -> x * y }
            else -> throw RuntimeException("Unexpected operation")
        }
    }


    fun runProgram(machine: Machine): Machine =
        if (machine.operation == 99) machine
        else {
            runProgram(iterateOnce(machine))
        }

    private fun doOperation(machine: Machine, operation: (Int, Int) -> Int): Machine {
        val loc1 = machine.tape[machine.position + 1]
        val loc2 = machine.tape[machine.position + 2]
        val resultPosition = machine.tape[machine.position + 3]

        val newTape = if (resultPosition < machine.tape.size) {
            machine.tape
        } else {
            machine.tape + List(resultPosition - machine.tape.size + 1) { x -> x }
        }


        val updatedTape = newTape.mapIndexed { index, value ->
            if (index == resultPosition) operation(machine.tape[loc1], machine.tape[loc2])
            else value
        }

        return Machine(machine.position + 4, updatedTape)
    }

    fun tests() {
        val input = listOf(
            1, 9, 10, 3,
            2, 3, 11, 0,
            99, 30, 40, 50
        )

        val actual = iterateOnce(Machine(0, input)).tape
        assert(
            actual == listOf(
                1, 9, 10, 70,
                2, 3, 11, 0,
                99,
                30, 40, 50
            )
        ) { "Mismatch, actual: $actual" }

        val expected = listOf(
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

    private fun testProgram(input: List<Int>, expected: List<Int>) {
        val final = runProgram(Machine(0, input))
        assert(
            final.tape == expected
        ) { "Mismatch. Input: $input, expected $expected actual: $final" }
    }

    fun runWithNounAndVerb(
        inputs: List<Int>,
        noun: Int,
        verb: Int
    ): Int {
        val crashTape = inputs.mapIndexed { i, v ->
            when (i) {
                1 -> noun
                2 -> verb
                else -> v
            }
        }

        return runProgram(Machine(0, crashTape)).tape[0]
    }

    fun landOnMoon(inputs: List<Int>): Pair<Int, Int> {
        val verbs = 0.rangeTo(99)
        val nouns = 0.rangeTo(99)

        for (verb in verbs) for (noun in nouns) {
            val result = runWithNounAndVerb(inputs, noun, verb)
            if (result == 19690720) return Pair(noun, verb)
        }

        throw Exception("No match found")
    }
}

fun main() {

    tests()

    val inputs = File("resources/02-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toInt)

    val result = runWithNounAndVerb(inputs, 12, 2)
    println("Result $result")

    val result2 = landOnMoon(inputs)

    println("Final result: ${100 * result2.first + result2.second}")
}