import java.io.File

fun main() {
    val width = 25
    val height = 6
    val layers = File("resources/08-input")
        .readLines()
        .flatMap { line -> line.chunked(width * height) }
        .map { chunk -> chunk.map { c -> Character.getNumericValue(c) } }

    val sparsestLayer = layers.minBy { layer -> layer.count { x -> x == 0 } }.orEmpty()

    val ones = sparsestLayer.count { x -> x == 1 }
    val twos = sparsestLayer.count { x -> x == 2 }
    val checkSum = ones * twos

    println("Checksum $checkSum")

    decode(layers, width, height)
}


fun decode(layers: List<List<Int>>, width: Int, height: Int) {
    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            val pixel = layers.find{ l -> l[index] != 2 }!![index]
            print(if (pixel == 0) ' ' else '*')
        }

        print('\n')
    }
}
