package util

data class Location(val x: Long, val y: Long) {
    operator fun plus(other: Location): Location =
        Location(
            this.x + other.x,
            this.y + other.y
        )

    fun move(direction: Direction, invertY: Boolean = false): Location {
        return when (direction) {
            Direction.UP -> this + if (invertY) Location(0, -1) else Location(0, 1)
            Direction.DOWN -> this + if (invertY) Location(0, 1) else Location(0, -1)
            Direction.RIGHT -> this + Location(1, 0)
            Direction.LEFT -> this + Location(-1, 0)
        }
    }
}