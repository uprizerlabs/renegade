package renegade.datasets.mnist

import mu.KotlinLogging
import renegade.crossValidation.*
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.supervised.*
import java.util.zip.GZIPInputStream

/**
 * Created by ian on 7/16/17.
 */
private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val data = loadMnistDataset("mnist_train.csv.gz")
    val crossValidator = CrossValidator<IntArray, Int, Int>(SimpleSplitStrategy(0.1), CorrectClassificationProportion(), data)

    val builders = ArrayList<DistanceModelBuilder<IntArray>>()
    for (ix in data.first().first.indices) {
        builders += DoubleDistanceModelBuilder().map("$ix") { it[ix].toDouble() }
    }

    crossValidator.test { data ->
        val classifier = Classifier(data, builders)
        var count = 0
        val f = { inputs: IntArray ->
            count++
            if ((count and (count - 1) == 0)) {
                logger.info("Tested $count values")
            }
            classifier.predict(inputs).mostLikely()
        }
        f
    }
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