package renegade.datasets.mnist

import mu.KotlinLogging
import renegade.MetricSpace
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.opt.OptConfig
import renegade.supervised.Schemas
import renegade.supervised.VertexPointLearner
import renegade.supervised.WaypointLearner
import java.util.zip.GZIPInputStream
import kotlin.math.abs

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

        val builders = createMnistBuilders(trainingData)

        logger.info("Created ${builders.size} builders.")

        logger.info("Building classifier")

        val cfg = OptConfig()
        cfg[VertexPointLearner.Parameters.multithreadDistance] = false
        cfg[MetricSpace.Parameters.maxSamples] = 10_000
        cfg[MetricSpace.Parameters.maxModelCount] = Integer.MAX_VALUE
        cfg[MetricSpace.Parameters.maxIterations] = 10
        cfg[VertexPointLearner.Parameters.sampleSize] = 1000

        val schema = Schemas.ClassifierSchema<List<Double>, Int>()
        val waypointLearner = WaypointLearner(cfg, schema)

        val metric = MetricSpace(cfg, builders, trainingData) { a, b -> if (a == b) 0.0 else 1.0}

        logger.info("Starting wayppoint learner")

        val waypoint = waypointLearner.learn(metric, trainingData)
    }


}

fun createMnistBuilders(trainingData: List<Pair<List<Double>, Int>>): ArrayList<DistanceModelBuilder<List<Double>>> {
    val builders = ArrayList<DistanceModelBuilder<List<Double>>>()

    for (distanceModelBuilderIx in trainingData.first().first.indices) {
        builders += DoubleDistanceModelBuilder(label = "[$distanceModelBuilderIx]")
                .map { it[distanceModelBuilderIx] }
    }
    return builders
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