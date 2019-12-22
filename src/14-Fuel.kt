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

fun computeOre(
    reactions: Map<String, Reaction>,
    amount: Long
): Long {
    fun mergeWithSum(
        simple: Map<String, Long>,
        simpleElements: Map<String, Long>
    ): Map<String, Long> {
        return (simple.keys + simpleElements.keys).associateWith { k ->
            (simple[k] ?: 0) + (simpleElements[k] ?: 0)
        }
    }

    fun getNextNeeded(
        element: Pair<String, Long>,
        waste: Map<String, Long>
    ): Pair<Map<String, Long>, Map<String, Long>> {
        val reaction = reactions[element.first]!!
        val recyclableWaste = waste[element.first] ?: 0


        val (toProduce, wasteLeft) = if (recyclableWaste < element.second) {
            Pair(element.second - recyclableWaste, 0L)
        } else Pair(0L, recyclableWaste - element.second)

        val factor = computeFactor(toProduce, reaction.output.second.toLong())
        return if (factor == 0L) Pair(emptyMap(), mapOf(Pair(element.first, wasteLeft))) else {
            val newWaste = (factor * reaction.output.second) - toProduce
            val inputNeeded = reaction.input.map { Pair(it.first, it.second * factor) }

            return Pair(inputNeeded.toMap(), mapOf(Pair(element.first, newWaste)))
        }
    }


    tailrec fun rec(complex: Map<String, Long>, waste: Map<String, Long>): Long {
        return if (complex.all { it.key == "ORE" }) complex["ORE"]!! else {
            val nextElement: Pair<String, Long> = complex
                .filter { it.key != "ORE" }
                .entries.elementAt(0).toPair()
            val (nextNeeded, newWaste) = getNextNeeded(nextElement, waste)
            val newMap: Map<String, Long> = complex.minus(nextElement.first)
            val wasteUsed = waste - newWaste.keys
            rec(mergeWithSum(newMap, nextNeeded), mergeWithSum(wasteUsed, newWaste))
        }
    }

    return rec(mapOf(Pair("FUEL", amount)), emptyMap())
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

    val ore = computeOre(requirements, 1L)
    println("Required: $ore")

    val fuel = findMaximumFuel(1000000000000L, requirements, ore)
    println("Fuel possible: $fuel")
}

fun findMaximumFuel(inHold: Long, reactions: Map<String, Reaction>, oneRun: Long): Long {
    var part2 = inHold / (oneRun - 1)
    while (true) {
        val checkFuel = computeOre(reactions, part2)
        if (checkFuel <= inHold) {
            val sizingCheck = (inHold - checkFuel) / oneRun;
            part2 += when {
                sizingCheck > 10000000 -> 10000000
                sizingCheck > 1000000 -> 1000000
                sizingCheck > 100000 -> 100000
                sizingCheck > 10000 -> 10000
                sizingCheck > 1000 -> 1000
                sizingCheck > 100 -> 100
                sizingCheck > 10 -> 10
                else -> 1
            }
        } else {
            return part2 - 1
        }
    }
}
