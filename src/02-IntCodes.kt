import IntCodes.runProgram
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
}

fun main() {

    tests()

    val inputs = File("resources/02-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toInt)

    val crashTape = inputs.mapIndexed { i, v ->
        when (i) {
            1 -> 12
            2 -> 2
            else -> v
        }
    }

    val result = runProgram(Machine(0, crashTape)).tape[0]

    println("Result $result")
}