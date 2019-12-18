import computer.Machine
import computer.State
import java.io.File

fun main() {
    val program = File("resources/07-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toLong)

    val maxSignal = computePermutations(listOf(0, 1, 2, 3, 4))
        .map { ampSettings -> runAmplifiers(ampSettings, program)[4].output }
        .max()

    println("Max signal: $maxSignal")

    val maxSignalFeedbackLoop = computePermutations(listOf(5, 6, 7, 8, 9))
        .map { ampSettings -> runFeedback(ampSettings, program)[4].output }
        .max()

    println("Max signal feeback: $maxSignalFeedbackLoop")

}

fun runFeedback(ampSettings: List<Long>, program: List<Long>): List<Machine> {

    val amplifiers = ampSettings.map { setting ->
        Machine(program).run().input(setting)
    }

    return runFeedbackRecursive(amplifiers)
}

fun computePermutations(inputs: List<Long>): List<List<Long>> {
    return when {
        inputs.size <= 1 -> listOf(inputs)
        else -> {
            val current: ArrayList<List<Long>> = ArrayList()
            for (value in inputs) {
                val subPermutations = computePermutations(inputs - value)
                for (perm in subPermutations) {
                    val permWithValue = listOf(value) + perm
                    current += permWithValue
                }
            }

            current
        }
    }
}

private fun runFeedbackRecursive(
    amplifiers: List<Machine>
): List<Machine> {
    return if (amplifiers[0].state == State.Halt) amplifiers
    else {
        runFeedbackRecursive(runAmplifiersRecursive(amplifiers, amplifiers[4].output))
    }
}

private fun runAmplifiers(
    ampSettings: List<Long>,
    program: List<Long>
): List<Machine> {

    val amplifiers = ampSettings.map { setting ->
        Machine(program).run().input(setting)
    }

    return runAmplifiersRecursive(amplifiers, 0)
}

fun runAmplifiersRecursive(amplifiers: List<Machine>, lastOutput: Long): List<Machine> {
    val ampARun = runUntilOutput(amplifiers[0], lastOutput)
    val ampBRun = runUntilOutput(amplifiers[1], ampARun.output)
    val ampCRun = runUntilOutput(amplifiers[2], ampBRun.output)
    val ampDRun = runUntilOutput(amplifiers[3], ampCRun.output)
    val ampERun = runUntilOutput(amplifiers[4], ampDRun.output)
    return listOf(ampARun.cont(), ampBRun.cont(), ampCRun.cont(), ampDRun.cont(), ampERun.cont())
}

fun runUntilOutput(amplifier: Machine, input: Long): Machine {
    return when (amplifier.state) {
        State.Halt -> amplifier
        State.Running -> runUntilOutput(amplifier.run(), input)
        State.Input -> runUntilOutput(amplifier.input(input), input)
        State.Output -> amplifier

    }
}


