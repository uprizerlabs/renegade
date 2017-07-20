package dnn.datasets.mnist

import dnn.MetricSpace
import dnn.crossValidation.*
import dnn.distanceModelBuilder.*
import dnn.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import mu.KotlinLogging
import java.util.zip.GZIPInputStream

/**
 * Created by ian on 7/16/17.
 */
private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val data = loadMnistDataset("mnist_train.csv.gz")
    val ms = mnistMetricSpaceBuilder(data)
    println("Done")
    val crossValidator = CrossValidator<IntArray, Int, Int>(FoldSplitStrategy(5), CorrectClassificationProportion(), data)
}

fun mnistMetricSpaceBuilder(data: List<Pair<IntArray, Int>>): MetricSpace<IntArray, Int> {
    val builders = ArrayList<DistanceModelBuilder<IntArray>>()
    for (ix in data.first().first.indices) {
        builders += DoubleDistanceModelBuilder().map("$ix") { it[ix].toDouble() }
    }
    return MetricSpace(distanceModelBuilderList = builders.wrap(), trainingData = data, outputDistance = { a: Int, b: Int -> if (a == b) 0.0 else 1.0 }, maxSamples = 1000)

}

fun loadMnistDataset(name: String): List<Pair<IntArray, Int>> {
    return GZIPInputStream(Dummy::class.java.getResourceAsStream(name)).bufferedReader().useLines {
        it.map { line ->
            val columns = line.split(',')
            val label = columns[0].toInt()
            val pixels = columns.map(String::toInt).subList(1, columns.size).toIntArray()
            Pair(pixels, label)
        }.toList()
    }
}

class Dummy