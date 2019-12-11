import java.io.File

fun main() {
    val program = File("resources/07-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toLong)

    val maxSignal = computePermutations(listOf(0, 1, 2, 3, 4))
        .map { ampSettings ->
            runAmplifiers(ampSettings, program)
        }
        .max()

    println("Max signal: $maxSignal")
}

fun computePermutations(inputs: List<Int>): List<List<Int>> {
    return when {
        inputs.size <= 1 -> listOf(inputs)
        else -> {
            val current: ArrayList<List<Int>> = ArrayList()
            for (value in inputs) {
                val subPermutations: List<List<Int>> = computePermutations(inputs - value)
                for (perm in subPermutations) {
                    val permWithValue: List<Int> = listOf(value) + perm
                    current += permWithValue
                }
            }

            current
        }
    }
}

private fun runAmplifiers(
    ampSettings: List<Int>,
    program: List<Long>
): Long {
    var inputState = 0

    fun getInputFunction(ampSetting: Int, inputValue: Long): () -> Long = {
        if (inputState == 0) {
            inputState = 1
            ampSetting.toLong()
        } else {
            inputState = 0
            inputValue
        }
    }

    var finalResult: Long = -1
    /*val ampA = Machine(input = getInputFunction(ampSettings[0], 0), output = { signal1 ->
        val ampB = Machine(input = getInputFunction(ampSettings[1], signal1), output = { signal2 ->
            val ampC = Machine(input = getInputFunction(ampSettings[2], signal2), output = { signal3 ->
                val ampD = Machine(input = getInputFunction(ampSettings[3], signal3), output = { signal4 ->
                    val ampE = Machine(input = getInputFunction(ampSettings[4], signal4), output = {
                        finalResult = it
                    })
                    ampE.run(program)
                })
                ampD.run(program)
            })
            ampC.run(program)
        })
        ampB.run(program)
    })
        ampA.run(program)
*/
    return finalResult
}