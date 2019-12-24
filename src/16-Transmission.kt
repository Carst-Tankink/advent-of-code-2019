import java.io.File
import kotlin.math.absoluteValue

fun generatePattern(length: Int, position: Int): List<Int> {

    return listOf(0, 1, 0, -1)
        .flatMap { x -> List(position + 1) { x } }
        .repeat(length + 1)
        .drop(1)
}

fun main() {

    val inputSequence = File("resources/16-input")
        .readLines()
        .flatMap { s -> s.map { Character.getNumericValue(it) } }

    val patterns: Map<Int, List<Int>> = 0.until(inputSequence.size)
        .map { position -> Pair(position, generatePattern(inputSequence.size, position)) }
        .toMap()

    val result = runPhases(inputSequence, patterns, 100)

    println("First 8 digits: ${result.take(8).joinToString("")}")
}

fun runPhases(inputSequence: List<Int>, patterns: Map<Int, List<Int>>, i: Int): List<Int> {
    return if (i == 0) inputSequence else {
        val phaseResult = calculatePhase(inputSequence, patterns)
        runPhases(phaseResult,  patterns, i - 1)
    }
}

fun calculatePhase(inputSequence: List<Int>, patterns: Map<Int, List<Int>>): List<Int> {
    val size = inputSequence.size

    return List(size) { pos ->
        val pattern = patterns[pos]!!
        val x = inputSequence.zip(pattern)
            .map { (digit, pattern) -> digit * pattern }
            .sum()
        x.absoluteValue % 10
    }
}

fun <T> List<T>.repeat(targetLength: Int): List<T> {
    tailrec fun rec(acc: List<T>, source: List<T>): List<T> {
        return if (acc.size == targetLength) acc else {
            val head = source[0]
            rec(acc + head, source.drop(1) + head)
        }
    }

    return rec(emptyList(), this)
}