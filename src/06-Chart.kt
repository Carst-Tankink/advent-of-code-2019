import Chart.sumOrbits
import Chart.tests
import com.sun.tools.corba.se.idl.constExpr.Or
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

        val sumOrbits = sumOrbits(input)
        assert(sumOrbits == 42L) { "Was: $sumOrbits"}
    }

    fun sumOrbits(input: List<Orbit>): Long {
        val bySource = input.groupBy { it.from }

        return sumOrbitsRec(bySource["COM"].orEmpty(), bySource, 0L)
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
}

fun main() {
    tests()

    val orbits = File("resources/06-input")
        .readLines()
        .map { x -> x.split(')') }
        .map { o -> Orbit(o[0], o[1]) }

    val checkSum = sumOrbits(orbits)
    println("Total orbits: $checkSum")
}