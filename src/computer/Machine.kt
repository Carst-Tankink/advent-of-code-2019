package computer

sealed class State
object Halt: State()
object Input: State()
object Output: State()

data class Machine(
    val position: Int = 0,
    var input: Long = 0L,
    val output: Long = 0L,
    val relativeBase: Int = 0
) {
    enum class Operation(val code: Long) {
        ADD(1),
        MUL(2),
        SAVE(3),
        OUTPUT(4),
        JUMPIFTRUE(5),
        JUMPIFFALSE(6),
        LESSTHAN(7),
        EQUALS(8),
        ADJUST_BASE(9),
        HALT(99);

        companion object {
            fun fromCode(code: Long): Operation {
                return values().findLast { op -> op.code == (code % 100) }!!
            }
        }
    }

    private fun updateTape(
        resultPosition: Int,
        tape: List<Long>,
        result: Long
    ) = resizeTape(resultPosition, tape)
        .mapIndexed { index, value -> if (index == resultPosition) result else value }

    private fun resizeTape(resultPosition: Int, tape: List<Long>): List<Long> {
        return if (resultPosition < tape.size) {
            tape
        } else {
            tape + List(resultPosition - tape.size + 1) { x -> x.toLong() }
        }
    }

    private fun doAdjustBase(tape: List<Long>): Pair<Machine, List<Long>> {
        val baseAdjustment = readArgs(tape, 1)[0].toInt()
        return Pair(copy(position = position + 2, relativeBase = relativeBase + baseAdjustment), tape)
    }

    private fun doJump(tape: List<Long>, test: (Long) -> Boolean): Pair<Machine, List<Long>> {
        val params = readArgs(tape, 2)
        val newPosition = if (test(params[0])) params[1].toInt() else position + 3
        return Pair(copy(position = newPosition), tape)
    }

    private fun readArgsRec(
        tape: List<Long>,
        opcode: Long,
        count: Int,
        acc: List<Long>,
        totalArgs: Int
    ): List<Long> {
        return if (count == 0) {
            acc
        } else {
            val mode = opcode % 10
            val param = tape[position + (totalArgs - (count - 1))]
            val data = readData(mode.toInt(), tape, param)
            readArgsRec(tape, opcode / 10, count - 1, acc + data, totalArgs)
        }
    }

    private fun readArgs(tape: List<Long>, count: Int): List<Long> =
        readArgsRec(tape, tape[position] / 100, count, emptyList(), count)


    private fun doBinaryOperation(tape: List<Long>, operation: (Long, Long) -> Long): Pair<Machine, List<Long>> {
        val arguments = readArgs(tape, 2)

        val result = operation(arguments[0], arguments[1])
        val resultPosition = tape[position + 3]
        val updatedTape = updateTape(resultPosition.toInt(), tape, result)

        return Pair(copy(position = position + 4), updatedTape)
    }

    private fun readData(mode: Int, tape: List<Long>, value: Long): Long {
        return when (mode) {
            0 -> if (value < tape.size) tape[value.toInt()] else 0
            1 -> value
            2 -> {
                val index = relativeBase + value
                if (index < tape.size) tape[index.toInt()] else 0
            }
            else -> throw Exception("Unexpected mode: $mode")
        }
    }

    private fun doSave(tape: List<Long>): Pair<Machine, List<Long>> {
        val savePosition = tape[position + 1]
        val inputData = input
        return Pair(
            copy(position = position + 2),
            updateTape(savePosition.toInt(), tape, inputData)
        )

    }

    private fun doOutput(tape: List<Long>): Pair<Machine, List<Long>> {
        val outputData = readArgs(tape, 1)[0]
        return Pair(copy(position = position + 2, output = outputData), tape)
    }

    fun runSingle(tape: List<Long>): Pair<Machine, List<Long>> {
        return when (Operation.fromCode(tape[position])) {
            Operation.ADD -> doBinaryOperation(tape) { x, y -> x + y }
            Operation.MUL -> doBinaryOperation(tape) { x, y -> x * y }
            Operation.SAVE -> doSave(tape)
            Operation.OUTPUT -> doOutput(tape)
            Operation.JUMPIFTRUE -> doJump(tape) { x -> x != 0L }
            Operation.JUMPIFFALSE -> doJump(tape) { x -> x == 0L }
            Operation.LESSTHAN -> doBinaryOperation(tape) { x, y -> if (x < y) 1 else 0 }
            Operation.EQUALS -> doBinaryOperation(tape) { x, y -> if (x == y) 1 else 0 }
            Operation.ADJUST_BASE -> doAdjustBase(tape)
            Operation.HALT -> Pair(this, tape)
        }
    }

    fun runInternal(tape: List<Long>): Triple<Machine, List<Long>, State> {
        val opCode = tape[position]
        return when (Operation.fromCode(opCode)) {
            Operation.HALT -> Triple(this, tape, Halt)
            Operation.SAVE -> Triple(this, tape, Input)
            Operation.OUTPUT -> {
                val (newMachine, newTape) = runSingle(tape)
                Triple(newMachine, newTape, Output)
            }
            else -> {
                val (newMachine, newTape) = runSingle(tape)
                newMachine.runInternal(newTape)
            }
        }
    }

    fun input(input: Long, tape: List<Long>): Triple<Machine, List<Long>, State> {
        this.input = input
        val (newMachine, newTape) = doSave(tape)
        return newMachine.runInternal(newTape)
    }
    fun run(tape: List<Long>): List<Long> = runInternal(tape).second
}