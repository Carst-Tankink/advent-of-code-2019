import java.io.File
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

data class Asteroid(val x: Double, val y: Double)

fun getVisibileAsteroids(asteroid: Asteroid, allAsteroids: List<Asteroid>): List<Asteroid> {
    val normalizedByDistance = allAsteroids
        .map { Asteroid(it.x - asteroid.x, it.y - asteroid.y) }
        .sortedBy { sqrt(it.x.pow(2) + it.y.pow(2)) }

    return calculateLinesOfSight(asteroid, emptyList(), normalizedByDistance)
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
            line.mapIndexed { x, token -> if (token == '#') Asteroid(x.toDouble(), y.toDouble()) else null }.filterNotNull()
        }
        .flatten()

    val mostVisible = asteroids
        .map { getVisibileAsteroids(it, asteroids - it) }
        .maxBy { it.size }

    println("Most visible: ${mostVisible!!.size}")
}