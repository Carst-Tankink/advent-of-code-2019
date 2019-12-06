package computer

data class Machine(val position: Int = 0) {
    enum class Operation(val code: Int) {
        ADD(1),
        MUL(2),
        SAVE(3),
        OUTPUT(4),
        HALT(99);

        companion object {
            fun fromCode(code: Int): Operation {
                return values().findLast { op -> op.code == (code % 100) }!!
            }
        }
    }


    private fun updateTape(
        resultPosition: Int,
        tape: List<Int>,
        result: Int
    ) = resizeTape(resultPosition, tape)
        .mapIndexed { index, value -> if (index == resultPosition) result else value }

    private fun resizeTape(
        resultPosition: Int, tape: List<Int>
    ): List<Int> {
        return if (resultPosition < tape.size) {
            tape
        } else {
            tape + List(resultPosition - tape.size + 1) { x -> x }
        }
    }

    private fun runInternal(tape: List<Int>): Pair<Machine, List<Int>> {
        val opCode = tape[position]
        return when (Operation.fromCode(opCode)) {
            Operation.HALT -> Pair(this, tape)
            else -> {
                val (newMachine, newTape) = runSingle(tape)
                newMachine.runInternal(newTape)
            }
        }
    }

    fun runSingle(tape: List<Int>): Pair<Machine, List<Int>> {
        return when (Operation.fromCode(tape[position])) {
            Operation.ADD -> doBinaryOperation(tape) { x, y -> x + y }
            Operation.MUL -> doBinaryOperation(tape) { x, y -> x * y }
            Operation.SAVE -> doSave(tape)
            Operation.OUTPUT -> doOutput(tape)
            Operation.HALT -> Pair(this, tape)
        }
    }

    private fun doBinaryOperation(tape: List<Int>, operation: (Int, Int) -> Int): Pair<Machine, List<Int>> {
        val mode1 = (tape[position] / 100) % 10
        val mode2 = (tape[position] / 1000) % 10
        val value1 = tape[position + 1]
        val value2 = tape[position + 2]

        val op1 = readData(mode1, tape, value1)
        val op2 = readData(mode2, tape, value2)

        val result = operation(op1, op2)
        val resultPosition = tape[position + 3]
        val updatedTape = updateTape(resultPosition, tape, result)

        return Pair(copy(position = position + 4), updatedTape)
    }

    private fun readData(mode1: Int, tape: List<Int>, value1: Int): Int {
        return when (mode1) {
            0 -> tape[value1]
            1 -> value1
            else -> -42
        }
    }

    private fun doSave(tape: List<Int>): Pair<Machine, List<Int>> {
        val savePosition = tape[position + 1]
        println("Please provide input: ")
        val input = readLine()?.toInt()!!

        return Pair(Machine(position + 2), updateTape(savePosition, tape, input))

    }

    private fun doOutput(tape: List<Int>): Pair<Machine, List<Int>> {
        val mode = (tape[position] / 100) % 10
        val output = readData(mode, tape, tape[position + 1])

        println("Output: $output")
        return Pair(Machine(position + 2), tape)
    }

    fun run(tape: List<Int>): List<Int> = runInternal(tape).second
}