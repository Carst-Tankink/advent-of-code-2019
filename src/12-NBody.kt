import java.io.File
import kotlin.math.absoluteValue

data class Vector(val x: Int, val y: Int, val z: Int) {
    fun absoluteValue(): Vector = Vector(x.absoluteValue, y.absoluteValue, z.absoluteValue)

    fun sum(): Long = x.toLong() + y + z
    operator fun plus(other: Vector): Vector = Vector(this.x + other.x, this.y + other.y, this.z + other.z)

    private fun compareCoordinate(coordinate1: Int, coordinate2: Int): Int = when {
        coordinate1 < coordinate2 -> 1
        coordinate1 > coordinate2 -> -1
        else -> 0
    }

    fun compare(other: Vector): Vector =
        Vector(compareCoordinate(x, other.x), compareCoordinate(y, other.y), compareCoordinate(z, other.z))

    override fun toString(): String {
        return "<x=$x, y=$y, z=$z>"
    }
}

data class Moon(val position: Vector, val velocity: Vector = Vector(0, 0, 0)) {
    private fun potentialEnergy(): Long = position.absoluteValue().sum()

    private fun kineticEnergy(): Long = velocity.absoluteValue().sum()

    fun energy(): Long = potentialEnergy() * kineticEnergy()
    fun move(): Moon {
        return copy(position = position + velocity)
    }

    companion object {
        fun fromString(input: String): Moon {
            val regex = "<x=([^,]+), y=([^,]+), z=([^>]+)>".toRegex()
            return regex.matchEntire(input)
                ?.destructured
                ?.let { (xVal, yVal, zVal) ->
                    Moon(Vector(xVal.toInt(), yVal.toInt(), zVal.toInt()))
                } ?: throw IllegalArgumentException("Bad input $input")
        }
    }

}

fun calculateEnergy(system: List<Moon>): Long = system.map { moon -> moon.energy() }.sum()

fun applyGravity(system: List<Moon>): List<Moon> {
    return system.map { moon ->
        val newVelocity: Vector = (system - moon)
            .map { it.position }
            .fold(moon.velocity) { acc, otherPosition ->
                val difference = moon.position.compare(otherPosition)
                acc + difference
            }
        Moon(moon.position, newVelocity)
    }
}

tailrec fun simulate(
    system: List<Moon>,
    steps: Int
): List<Moon> {
    return if (steps == 0) system else {
        simulate(step(system), steps - 1)
    }
}

private fun step(system: List<Moon>): List<Moon> {
    val withNewVelocity: List<Moon> = applyGravity(system)
    val withNewPositions: List<Moon> = withNewVelocity
        .map { moon -> moon.move() }
    return withNewPositions
}

fun simulateUntilSame(initialSystem: List<Moon>): Map<String, Long> {
    val periods: Map<String, Long> = emptyMap()
    return simulatePeriods(periods, initialSystem, 1, initialSystem)
}

tailrec fun simulatePeriods(
    periods: Map<String, Long>,
    state: List<Moon>,
    step: Long,
    initialSystem: List<Moon>
): Map<String, Long> {
    return if (periods.size == 3) periods else {
        val newState = step(state)

        val getX = { moon: Moon -> Pair(moon.position.x, moon.velocity.x) }
        val withX = if (equalOnCoordinate(newState, getX, initialSystem)) {
            periods + Pair("x", step)
        } else periods

        val getY = { moon: Moon -> Pair(moon.position.y, moon.velocity.y) }
        val withY = if (equalOnCoordinate(newState, getY, initialSystem)) {
            withX + Pair("y", step)
        } else withX

        val getZ = { moon: Moon -> Pair(moon.position.z, moon.velocity.z) }
        val withZ = if (equalOnCoordinate(newState, getZ, initialSystem)) {
            withY + Pair("z", step)
        } else withY

        simulatePeriods(withZ, newState, step + 1, initialSystem)
    }
}

private fun equalOnCoordinate(
    newState: List<Moon>,
    getCoordinates: (Moon) -> Pair<Int, Int>,
    initialSystem: List<Moon>
): Boolean {
    val xFactors = newState.map(getCoordinates)
    val xOld = initialSystem.map(getCoordinates)
    return xFactors.zip(xOld).all { it.first.first == it.second.first && it.first.second == it.second.second }
}

fun main() {
    val system = File("resources/12-input")
        .readLines()
        .map { Moon.fromString(it) }

    val steps = 1000
    calculatePairs(system)

    val inMotion = simulate(system, steps)
    val energy = calculateEnergy(inMotion)
    println("Energy: $energy")

    val stepsUntilSame: Map<String, Long> = simulateUntilSame(system)

    println("Steps per system $stepsUntilSame")
}

private fun calculatePairs(system: List<Moon>): List<Pair<Moon, Moon>> {
    return system
        .flatMap { moon -> system.map { otherMoon -> Pair(moon, otherMoon) } }
        .filterNot { it.first == it.second }
        .fold(emptyList()) { acc, pair -> if (acc.contains(flip(pair))) acc else acc + pair }
}

private fun flip(pair: Pair<Moon, Moon>) = Pair(pair.second, pair.first)
