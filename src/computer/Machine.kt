package computer

enum class State {
    Halt, Input, Output, Running
}

data class Machine(
    val memory: List<Long>,
    val position: Int = 0,
    val output: Long = 0L,
    val relativeBase: Int = 0,
    val state: State = State.Running
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

    private fun updateMemory(
        resultPosition: Int,
        result: Long
    ) = resizeMemory(resultPosition)
        .mapIndexed { index, value -> if (index == resultPosition) result else value }

    private fun resizeMemory(resultPosition: Int): List<Long> {
        return if (resultPosition < memory.size) {
            memory
        } else {
            memory + List(resultPosition - memory.size + 1) { x -> x.toLong() }
        }
    }

    private fun doAdjustBase(): Machine {
        val baseAdjustment = readArgs(1)[0].toInt()
        return copy(position = position + 2, relativeBase = relativeBase + baseAdjustment)
    }

    private fun doJump(test: (Long) -> Boolean): Machine {
        val params = readArgs(2)
        val newPosition = if (test(params[0])) params[1].toInt() else position + 3
        return copy(position = newPosition)
    }

    private fun readArgsRec(
        opcode: Long,
        count: Int,
        acc: List<Long>,
        totalArgs: Int
    ): List<Long> {
        return if (count == 0) {
            acc
        } else {
            val mode = opcode % 10
            val param = memory[position + (totalArgs - (count - 1))]
            val data = readData(mode.toInt(), param)
            readArgsRec(opcode / 10, count - 1, acc + data, totalArgs)
        }
    }

    private fun readArgs(count: Int): List<Long> =
        readArgsRec(memory[position] / 100, count, emptyList(), count)


    private fun doBinaryOperation(operation: (Long, Long) -> Long): Machine {
        val arguments = readArgs(2)

        val result = operation(arguments[0], arguments[1])
        val resultPosition = memory[position + 3]
        val updatedMemory = updateMemory(resultPosition.toInt(), result)

        return copy(memory = updatedMemory, position = position + 4)
    }

    private fun readData(mode: Int, value: Long): Long {
        return when (mode) {
            0 -> if (value < memory.size) memory[value.toInt()] else 0
            1 -> value
            2 -> {
                val index = relativeBase + value
                if (index < memory.size) memory[index.toInt()] else 0
            }
            else -> throw Exception("Unexpected mode: $mode")
        }
    }

    private fun doSave(input: Long): Machine {
        val savePosition = memory[position + 1]
        val updatedMemory = updateMemory(savePosition.toInt(), input)
        return copy(memory = updatedMemory, position = position + 2, state = State.Running)
    }

    private fun doOutput(): Machine {
        val outputData = readArgs(1)[0]
        return copy(position = position + 2, output = outputData, state = State.Output)
    }

    fun runSingle(): Machine {
        return when (Operation.fromCode(memory[position])) {
            Operation.ADD -> doBinaryOperation { x, y -> x + y }
            Operation.MUL -> doBinaryOperation { x, y -> x * y }
            Operation.SAVE -> copy(state = State.Input)
            Operation.OUTPUT -> doOutput()
            Operation.JUMPIFTRUE -> doJump { x -> x != 0L }
            Operation.JUMPIFFALSE -> doJump { x -> x == 0L }
            Operation.LESSTHAN -> doBinaryOperation { x, y -> if (x < y) 1 else 0 }
            Operation.EQUALS -> doBinaryOperation { x, y -> if (x == y) 1 else 0 }
            Operation.ADJUST_BASE -> doAdjustBase()
            Operation.HALT -> copy(state = State.Halt)
        }
    }

    fun run(): Machine {
        val result = runSingle()
        return if (result.state != State.Running) result else result.run()
    }

    fun input(input: Long): Machine {
        return doSave(input).run()
    }

    fun output(): Machine {
        return copy(state = State.Running).run()
    }
}