package renegade.datasets.mnist

import renegade.MetricSpace
import renegade.crossValidation.*
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.indexes.waypoint.WaypointIndex
import renegade.util.Two
import mu.KotlinLogging
import java.util.zip.GZIPInputStream

/**
 * Created by ian on 7/16/17.
 */
private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val data = loadMnistDataset("mnist_train.csv.gz")
    val crossValidator = CrossValidator<IntArray, Int, Int>(SimpleSplitStrategy(0.1), CorrectClassificationProportion(), data)

    crossValidator.test { data ->
        val metricSpace = mnistMetricSpaceBuilder(data)
        val msIndex = WaypointIndex<Pair<IntArray, Int?>>({ metricSpace.estimateDistance(Two(it.first.first, it.second.first)) }, 8, data)
        data.forEach { msIndex.add(it) }
        var count = 0
        val f = { inputs: IntArray ->
            count++
            if ((count and (count - 1) == 0)) {
                logger.info("Tested $count values")
            }
            msIndex.searchFor(inputs to null).first().item.second!!
        }
        f
    }
}

fun mnistMetricSpaceBuilder(data: List<Pair<IntArray, Int>>): MetricSpace<IntArray, Int> {
    val builders = ArrayList<DistanceModelBuilder<IntArray>>()
    for (ix in data.first().first.indices) {
        builders += DoubleDistanceModelBuilder().map("$ix") { it[ix].toDouble() }
    }
    val identity: (Int, Int) -> Double = { a, b -> if (a == b) 1.0 else 0.0 }
    return MetricSpace(modelBuilders = builders, trainingData = data, outputDistance = { a: Int, b: Int -> if (a == b) 0.0 else 1.0 }, maxSamples = 10000, learningRate = 0.01)

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