package util

enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    companion object {
        fun parse(char: Char): Direction {
            return when (char) {
                'U' -> UP
                'D' -> DOWN
                'L' -> LEFT
                'R' -> RIGHT
                else -> throw Exception("Unexpected input $char")
            }
        }
    }
}