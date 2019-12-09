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

    val decoded = decode(layers, width, height)
    printDecoded(decoded, width)
}


fun decode(layers: List<List<Int>>, width: Int, height: Int): List<Int> {
    val initial: List<Int> = List(width * height) { 2 }
    return layers.fold(initial) {acc, layer -> acc.zip(layer) {x, y -> if (x == 2) y else x} }
}

private fun printDecoded(decoded: List<Int>, width: Int) {
    decoded
        .map { if (it == 0) ' ' else '*' }
        .chunked(width)
        .map { it.toCharArray() }
        .forEach{println(it)}
}
