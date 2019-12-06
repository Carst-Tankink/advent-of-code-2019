package computer

data class Machine(val position: Int = 0) {
    enum class Operation(val code: Int) {
        ADD(1),
        MUL(2),
        SAVE(3),
        OUTPUT(4),
        JUMPIFTRUE(5),
        JUMPIFFALSE(6),
        LESSTHAN(7),
        EQUALS(8),
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
            Operation.JUMPIFTRUE -> doJump(tape) { x -> x != 0 }
            Operation.JUMPIFFALSE -> doJump(tape) { x -> x == 0 }
            Operation.LESSTHAN -> doBinaryOperation(tape) { x, y -> if (x < y) 1 else 0 }
            Operation.EQUALS -> doBinaryOperation(tape) { x, y -> if (x == y) 1 else 0 }
            Operation.HALT -> Pair(this, tape)
        }
    }

    private fun doJump(tape: List<Int>, test: (Int) -> Boolean): Pair<Machine, List<Int>> {
        val params = readArgs(tape, 2)
        val newPosition = if (test(params[0])) params[1] else position + 3
        return Pair(Machine(newPosition), tape)
    }

    private fun readArgsRec(
        tape: List<Int>,
        opcode: Int,
        count: Int,
        acc: List<Int>,
        totalArgs: Int
    ): List<Int> {
        return if (count == 0) {
            acc
        } else {
            val mode = opcode % 10
            val param = tape[position + (totalArgs - (count - 1))]
            val data = readData(mode, tape, param)
            readArgsRec(tape, opcode / 10, count - 1, acc + data, totalArgs)
        }
    }

    private fun readArgs(tape: List<Int>, count: Int): List<Int> =
        readArgsRec(tape, tape[position] / 100, count, emptyList(), count)


    private fun doBinaryOperation(tape: List<Int>, operation: (Int, Int) -> Int): Pair<Machine, List<Int>> {
        val arguments = readArgs(tape, 2)

        val result = operation(arguments[0], arguments[1])
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
        val output = readArgs(tape, 1)[0]
        println("Output: $output")
        return Pair(Machine(position + 2), tape)
    }

    fun run(tape: List<Int>): List<Int> = runInternal(tape).second
}