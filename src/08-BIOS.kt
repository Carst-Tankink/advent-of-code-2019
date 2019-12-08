import java.io.File

fun main() {
    val width = 25
    val height = 6
    val layers = File("resources/08-input")
        .readLines()
        .flatMap { line -> line.chunked(width * height)}
        .map { chunk -> chunk.map { c -> Character.getNumericValue(c) } }

    val sparsestLayer = layers.minBy { layer -> layer.count { x -> x == 0 } }.orEmpty()

    val ones = sparsestLayer.count { x -> x == 1 }
    val twos = sparsestLayer.count { x -> x == 2 }
    val checkSum = ones * twos

    println("Checksum $checkSum")
}