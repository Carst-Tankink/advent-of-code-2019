import computer.Machine
import java.io.File

fun main() {
    val _program = File("resources/07-input")
        .readLines()
        .flatMap { x -> x.split(",") }
        .map(String::toInt)

    val program = listOf(
        3,23,3,24,1002,24,10,24,1002,23,-1,23,
        101,5,23,23,1,24,23,23,4,23,99,0,0
    )
    val ampSettings = listOf(0,1,2,3,4)

    runAmplifiers(ampSettings, program)
}

private fun runAmplifiers(
    ampSettings: List<Int>,
    program: List<Int>
) {
    var inputState = 0

    fun getInputFunction(ampSetting: Int, inputValue: Int): () -> Int = {
        if (inputState == 0) {
            inputState = 1
            ampSetting
        } else {
            inputState = 0
            inputValue
        }
    }

    val ampA = Machine(input = getInputFunction(ampSettings[0], 0), output = { signal1 ->
        val ampB = Machine(input = getInputFunction(ampSettings[1], signal1), output = { signal2 ->
            val ampC = Machine(input = getInputFunction(ampSettings[2], signal2), output = { signal3 ->
                val ampD = Machine(input = getInputFunction(ampSettings[3], signal3), output = { signal4 ->
                    val ampE = Machine(input = getInputFunction(ampSettings[4], signal4))
                    ampE.run(program)
                })
                ampD.run(program)
            })
            ampC.run(program)
        })
        ampB.run(program)
    })
    ampA.run(program)
}