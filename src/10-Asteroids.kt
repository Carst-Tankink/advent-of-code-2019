import java.io.File

data class Asteroid(val x: Int, val y: Int)

fun main() {
    val asteroids: List<Asteroid> = File("resources/10-input")
        .readLines()
        .mapIndexed { y, line ->
            line.mapIndexed { x, token -> if (token == '#') Asteroid(x, y) else null }.filterNotNull()
        }
        .flatten()

    println("Asteroids: ${asteroids.size}")
    println("Asteroid 11: ${asteroids[11]}")
}