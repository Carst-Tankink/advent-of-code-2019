import computer.Machine
import computer.State
import java.io.File

fun main() {
    val codes: List<Long> = File("resources/05-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toLong)

    runRecursive(Machine(codes), 1)
    runRecursive(Machine(codes), 5)
}

fun runRecursive(machine: Machine, input: Long) {
    when (machine.state) {
        State.Halt -> return
        State.Input -> {
            val newMachine = machine.input(input)
            runRecursive(newMachine, input)
        }
        State.Output -> {
            println("OUT: ${machine.output}")
            val newMachine = machine.output()
            runRecursive(newMachine, input)
        }
        State.Running -> {
            val newMachine = machine.run()
            runRecursive(newMachine, input)
        }
    }
}
