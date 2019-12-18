package util

data class Location(val x: Int, val y: Int) {
    operator fun plus(other: Location): Location =
        Location(
            this.x + other.x,
            this.y + other.y
        )

    fun move(direction: Direction): Location {
        return when (direction) {
            Direction.UP -> this + Location(0, 1)
            Direction.DOWN -> this + Location(0, -1)
            Direction.RIGHT -> this + Location(1, 0)
            Direction.LEFT -> this + Location(-1, 0)
        }
    }
}