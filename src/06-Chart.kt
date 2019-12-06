import Chart.calculateTransfers
import Chart.sumOrbits
import Chart.tests
import java.io.File

data class Orbit(val from: String, val to: String)
object Chart {

    fun tests() {
        val input: List<Orbit> = listOf(
            Orbit("COM", "B"),
            Orbit("B", "C"),
            Orbit("C", "D"),
            Orbit("D", "E"),
            Orbit("E", "F"),
            Orbit("B", "G"),
            Orbit("G", "H"),
            Orbit("D", "I"),
            Orbit("E", "J"),
            Orbit("J", "K"),
            Orbit("K", "L")
        )

        val sumOrbits = sumOrbits(input.groupBy { it.from })
        assert(sumOrbits == 42L) { "Was: $sumOrbits" }


        val pathsInput = input + listOf(Orbit("K", "YOU"), Orbit("I", "SAN"))
        val transfers = calculateTransfers(pathsInput)
        assert(transfers == 4L) { "Was: $transfers" }

    }

    private fun sumOrbitsRec(targets: List<Orbit>, bySource: Map<String, List<Orbit>>, currentDistance: Long): Long {
        return if (targets.isEmpty()) {
            currentDistance
        } else {
            currentDistance + targets
                .map { sumOrbitsRec(bySource[it.to].orEmpty(), bySource, currentDistance + 1) }
                .sum()
        }
    }

    fun sumOrbits(input: Map<String, List<Orbit>>): Long {
        return sumOrbitsRec(input["COM"].orEmpty(), input, 0L)
    }

    fun calculateTransfers(map: List<Orbit>): Long {
        val backtracks = map.groupBy { it.to }
            .filterValues { v -> v.size == 1 }
            .mapValues { v -> v.value[0] }

        val backtracksForSanta = findBacktracks("SAN", backtracks, mapOf(Pair("SAN", 0L)))
        return backtrackToCommon("YOU", backtracks, backtracksForSanta, 0) - 2 // -2 to correct for initial hops YOU and SAN
    }

    private fun backtrackToCommon(
        s: String,
        backtracks: Map<String, Orbit>,
        backtracksForSanta: Map<String, Long>,
        i: Long
    ): Long {
        return if (backtracksForSanta.containsKey(s)) {
            backtracksForSanta[s]!! + i
        } else {
            val source = backtracks[s]?.from!!
            backtrackToCommon(source, backtracks, backtracksForSanta, i + 1)
        }
    }

    private fun findBacktracks(s: String, backtracks: Map<String, Orbit>, acc: Map<String, Long>): Map<String, Long> {
        return if (s == "COM") {
            acc
        } else {
            val currentDistance = acc[s]!!
            val source = backtracks[s]?.from!!
            findBacktracks(source, backtracks, acc + Pair(source, currentDistance + 1))
        }

    }

}

fun main() {
    tests()

    val orbits = File("resources/06-input")
        .readLines()
        .map { x -> x.split(')') }
        .map { o -> Orbit(o[0], o[1]) }

    val checkSum = sumOrbits(orbits.groupBy { it.from })
    println("Total orbits: $checkSum")

    val transfers = calculateTransfers(orbits)
    println("Total transfers: $transfers")
}