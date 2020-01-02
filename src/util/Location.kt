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

fun printGrid(grid: Map<Location, Any>) {
    val lines: Map<Long, List<Any>> = grid.entries
        .groupBy { it.key.y }
        .toSortedMap()
        .mapValues { locationLine ->
            locationLine.value
                .sortedBy { entry -> entry.key.x }
                .map { it.value }
        }

    for (line in lines) {
        for (item in line.value) {
            print(item.toString())
        }
        println()
    }
}