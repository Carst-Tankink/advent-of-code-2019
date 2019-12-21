import java.io.File

data class Reaction(val input: List<Pair<String, Int>>, val output: Pair<String, Int>) {
    companion object {
        fun fromString(string: String): Reaction {
            val parts = string.split(" => ")

            val inputs = parts[0].split(",")
                .map { it.trim() }
                .map { toChemical(it) }

            val output = toChemical(parts[1].trim())

            return Reaction(inputs, output)
        }

        private fun toChemical(string: String): Pair<String, Int> {
            val pattern = "(\\d+) ([A-Z]+)".toRegex()
            return pattern.matchEntire(string)
                ?.destructured
                ?.let { (amount, name) ->
                    Pair(name, amount.toInt())
                } ?: throw IllegalArgumentException("Bad input $string")
        }
    }
}

fun computeFactor(result: Long, a: Long): Long {
    tailrec fun rec(times: Long): Long = if (times * a >= result) times else rec(times + 1)

    return rec(0)
}

fun calculateOre(toMake: Map<String, Long>, reactions: Map<String, Reaction>): Long {
    return toMake.map {
        val reaction = reactions[it.key]!!
        val factor = computeFactor(it.value, reaction.output.second.toLong())
        reaction.input[0].second * factor
    }
        .sum()
}

fun computeOre(reactions: Map<String, Reaction>): Long {
    fun mergeWithSum(
        simple: Map<String, Long>,
        simpleElements: Map<String, Long>
    ): Map<String, Long> {
        return (simple.keys + simpleElements.keys).associateWith { k ->
            (simple[k] ?: 0) + (simpleElements[k] ?: 0)
        }
    }

    fun computeNext(complex: Map<String, Long>): Pair<Map<String, Long>, Map<String, Long>> {
        val neededInputs = complex.map { entry ->
            val inputReactions = reactions[entry.key]!!
            val factor = computeFactor(entry.value, inputReactions.output.second.toLong())
            inputReactions.input.map { Pair(it.first, it.second * factor) }
        }

        return neededInputs.fold(Pair(emptyMap(), emptyMap())) { acc, list ->
            val (newSimple, newComplex) = list.partition {
                reactions[it.first]?.input?.get(0)?.first == "ORE"
            }
            Pair(mergeWithSum(acc.first, newComplex.toMap()), mergeWithSum(acc.second, newSimple.toMap()))
        }
    }


    tailrec fun rec(complex: Map<String, Long>, simple: Map<String, Long>): Long {
        return if (complex.isEmpty()) calculateOre(simple, reactions) else {
            val (nextComplex, simpleElements) = computeNext(complex)
            val nextSimple = mergeWithSum(simple, simpleElements)
            rec(nextComplex, nextSimple)
        }
    }

    return rec(mapOf(Pair("FUEL", 1L)), emptyMap())
}

fun main() {
    val reactions: List<Reaction> = File("resources/14-input")
        .readLines()
        .map { Reaction.fromString(it) }

    val requirements = reactions.groupBy { it.output.first }
        .mapValues { entry ->
            if (entry.value.size > 1) {
                println("More than one input")
            }
            entry.value[0]
        }

    val ore = computeOre(requirements)
    println("Required: $ore")

}
