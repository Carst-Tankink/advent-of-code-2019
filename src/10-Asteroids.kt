import java.io.File
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

data class Asteroid(val x: Double, val y: Double)

fun getVisibileAsteroids(asteroid: Asteroid, allAsteroids: List<Asteroid>): Pair<Asteroid, List<Asteroid>> {
    val normalizedByDistance = allAsteroids
        .map { Asteroid(it.x - asteroid.x, it.y - asteroid.y) }
        .sortedBy { sqrt(it.x.pow(2) + it.y.pow(2)) }

    return Pair(asteroid, calculateLinesOfSight(asteroid, emptyList(), normalizedByDistance))
}

tailrec fun calculateLinesOfSight(
    asteroid: Asteroid,
    resultSet: List<Asteroid>,
    othersByDistance: List<Asteroid>
): List<Asteroid> {
    return if (othersByDistance.isEmpty()) resultSet else {
        val closest = othersByDistance[0]
        val theta = atan2(closest.y, closest.x)
        val filteredCandidates = (othersByDistance - closest)
            .filter { !hasAngle(it, theta) }
        calculateLinesOfSight(asteroid, resultSet + closest, filteredCandidates)
    }
}

fun hasAngle(toTest: Asteroid, theta: Double): Boolean {
    return atan2(toTest.y, toTest.x) == theta
}

fun main() {
    val asteroids: List<Asteroid> = File("resources/10-input")
        .readLines()
        .mapIndexed { y, line ->
            line.mapIndexed { x, token -> if (token == '#') Asteroid(x.toDouble(), y.toDouble()) else null }
                .filterNotNull()
        }
        .flatten()

    val mostVisible = asteroids
        .map { getVisibileAsteroids(it, asteroids - it) }
        .maxBy { it.second.size }!!
    println("Most visible at ${mostVisible.first}: ${mostVisible.second.size}")

    val location = mostVisible.first
    val fromOptimalLocation: Map<Double, List<Asteroid>> = (asteroids - location)
        .map { Asteroid(it.x - location.x, it.y - location.y) }
        .sortedBy { sqrt(it.x.pow(2) + it.y.pow(2)) }
        .groupBy { atan2(it.y, it.x) }
        .mapKeys { it.key + .5 * PI }
        .mapKeys { if (it.key < 0) it.key + 2 * PI else it.key }
        .toSortedMap()

    val order = vaporizeOrder(fromOptimalLocation)

    val target200 = Asteroid(order[199].x + location.x, order[199].y + location.y)
// !!! Add location back to found target
    println("First vaporized: $target200")

    println("Magic number: ${target200.x * 100 + target200.y}")


}

fun vaporizeOrder(fromOptimalLocation: Map<Double, List<Asteroid>>): List<Asteroid> {
    fun removeEntries(fromOptimalLocation: Map<Double, List<Asteroid>>): Map<Double, List<Asteroid>> {
        return fromOptimalLocation.mapValues { it.value.drop(1) }
            .filterValues { it.isNotEmpty() }
    }

    tailrec fun passes(acc: List<Asteroid>, asteroidsLeft: Map<Double, List<Asteroid>>): List<Asteroid> {
        return if (asteroidsLeft.isEmpty()) acc else {
            val pass: List<Asteroid> =
                asteroidsLeft.entries.fold(emptyList()) { accc, entry -> accc + entry.value[0] }
            val left = removeEntries(asteroidsLeft)
            return passes(acc + pass, left)
        }
    }

    return passes(emptyList(), fromOptimalLocation)
}



