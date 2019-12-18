package util

enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    fun rotate(direction: Direction): Direction {
        return when (direction) {
            LEFT -> {
                this.rotateLeft()
            }
            RIGHT -> {
                this.rotateRight()
            }
            else -> throw NotImplementedError("Turning not implemented")
        }
    }

    private fun rotateRight(): Direction {
        return when (this) {
            UP -> RIGHT
            DOWN -> LEFT
            LEFT -> UP
            RIGHT -> DOWN
        }
    }

    private fun rotateLeft(): Direction {
        return when (this) {
            UP -> LEFT
            DOWN -> RIGHT
            LEFT -> DOWN
            RIGHT -> UP
        }
    }

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