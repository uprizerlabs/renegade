package renegade.datasets.mnist

import mu.KotlinLogging
import renegade.MetricSpace
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.opt.OptConfig
import renegade.supervised.VertexPointLearner
import renegade.supervised.classification.Classifier
import java.util.zip.GZIPInputStream

private val logger = KotlinLogging.logger {}

fun main() {
    MNist()
}

class MNist {

    init {
        logger.info("Loading training data")
        val trainingData: List<Pair<List<Double>, Int>> = loadMnistDataset("mnist_train.csv.gz")
                .map { Pair(it.first.map { it.toDouble() }, it.second) }
        logger.info("Loaded ${trainingData.size} instances of training data.")

        val builders = ArrayList<DistanceModelBuilder<List<Double>>>()

        for (distanceModelBuilderIx in trainingData.first().first.indices) {
            builders += DoubleDistanceModelBuilder(label = "[$distanceModelBuilderIx]")
                    .map { it[distanceModelBuilderIx] }
        }

        logger.info("Created ${builders.size} builders.")

        logger.info("Building classifier")

        val cfg = OptConfig()
        cfg[VertexPointLearner.Parameters.multithreadDistance] = false
        cfg[MetricSpace.Parameters.maxSamples] = 10_000
        cfg[MetricSpace.Parameters.maxModelCount] = Integer.MAX_VALUE
        cfg[VertexPointLearner.Parameters.sampleSize] = 1000
        val classifier = Classifier(cfg, trainingData, builders)
    }

    fun loadMnistDataset(name: String): List<Pair<IntArray, Int>> {
        return GZIPInputStream(MNist::class.java.getResourceAsStream(name)).bufferedReader().useLines {
            it.map { line ->
                val columns = line.split(',')
                val label = columns[0].toInt()
                val pixels = columns.map(String::toInt).subList(1, columns.size).toIntArray()
                Pair(pixels, label)
            }.toList()
        }
    }
}