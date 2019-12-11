import computer.*
import java.io.File

fun main() {
    val codes: List<Long> = File("resources/05-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toLong)


    var (machine, tape, state) = Machine().runInternal(codes)
// TODO: Make this recursive
    while (state != Halt) {
        when (state) {
            Input -> {
                println("Please provide input")
                val input = readLine()!!.toLong()
                val (newMachine, newTape, newState) = machine.input(input, codes)
                machine = newMachine
                tape = newTape
                state = newState
            }
            is Output -> {
                println(machine.output)
                val machineState = machine.runInternal(tape)
                machine = machineState.first
                tape = machineState.second
                state = machineState.third
            }
        }
    }
}