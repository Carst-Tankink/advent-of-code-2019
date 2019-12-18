package util

data class Location(val x: Int, val y: Int) {
    operator fun plus(other: Location): Location =
        Location(
            this.x + other.x,
            this.y + other.y
        )
}