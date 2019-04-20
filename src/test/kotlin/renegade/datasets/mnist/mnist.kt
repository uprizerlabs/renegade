package renegade.datasets.mnist

import equalityOutputDistanceMetric
import mu.KotlinLogging
import renegade.MetricSpace
import renegade.crossValidation.*
import renegade.datasets.gen.spiral.Spiral
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.features.*
import renegade.indexes.waypoint.WaypointIndex
import renegade.opt.OptConfig
import renegade.supervised.*
import renegade.util.*
import java.util.zip.GZIPInputStream

/**
 * Created by ian on 7/16/17.
 */
private val logger = KotlinLogging.logger {}

val pairsToTest = 5
val maxSamples = 100000
val transformersToRetain = 5

fun main(args: Array<String>) {

    logger.info("Running test, pairsToTest: $pairsToTest, maxSamples: $maxSamples")

    logger.info("Loading dataset")

    val data: List<Pair<List<Double>, Int>> = Spiral().generate(samples = 100000)
     // loadMnistDataset("mnist_train.csv.gz").map { Pair(it.first.map { it.toDouble() }, it.second) }

    var currentFeatureTransformers: MutableList<FeatureExtractor> = ArrayList()

    for (ix in data.first().first.indices) {
        currentFeatureTransformers.add(RawFeature(ix))
    }

    logger.info("Beginning feature iteration")
    while (true) {
        val builders = ArrayList<DistanceModelBuilder<List<Double>>>()
        for (featureExtractor in currentFeatureTransformers) {
            builders += DoubleDistanceModelBuilder().map(featureExtractor.toString()) { featureExtractor.invoke(it)!! }
        }

        val metricSpace: MetricSpace<List<Double>, Int> = MetricSpace(OptConfig(), builders, data, outputDistance = equalityOutputDistanceMetric)

        logger.info("Cross-validating metricSpace")

        val crossValidator = CrossValidator<List<Double>, Int, Int>(SimpleSplitStrategy(0.1), CorrectClassificationProportion(), data)

        val cvScore = crossValidator.test { data ->
            val dist : (Two<Pair<List<Double>, Int?>>) -> Double = {metricSpace.invoke(Two(it.first.first, it.second.first))}
            val index = WaypointIndex<Pair<List<Double>, Int?>>(OptConfig(), distance = dist, samples = data)

            index.addAll(data)

            val classifier = SlowClassifier(index)
            var count = 0
            val f = { inputs: List<Double> ->
                count++
              /*  if ((count and (count - 1) == 0)) {
                    logger.info("Tested $count values")
                } */
                classifier.predict(inputs).mostLikely()
            }
            f
        }

        logger.info("Cross-validation score: $cvScore")

        val featureContributions = metricSpace.modelContributions.lastEntry().value

       // featureContributions.asSequence().sortedByDescending { it.value }.forEach { (ix, score) ->
       //     logger.info("${currentFeatureTransformers[ix]}: $score")
       // }

        val bestFeatureTransformer = featureContributions.entries.maxBy { it.value }!!
        val key = bestFeatureTransformer.key
        logger.info("Top feature contribution: ${currentFeatureTransformers[key]}, score: ${bestFeatureTransformer.value}")

        val transformersByContribution: List<FeatureExtractor> = currentFeatureTransformers.withIndex().map { (ix, fXformer) ->
            val contribution = featureContributions.get(ix)!!
            contribution to fXformer
        }.sortedByDescending { it.first }
                .map { it.second }

        val newTransformers = transformersByContribution.take(transformersToRetain).toMutableList()

        transformersByContribution.asSequence().toPairSequence().take(pairsToTest).forEach { combination ->
            newTransformers.addAll(generateBiops(combination.first, combination.second))
        }

        currentFeatureTransformers = newTransformers

    }

/*
    val crossValidator = CrossValidator<List<Double>, Int, Int>(SimpleSplitStrategy(0.1), CorrectClassificationProportion(), data)

    crossValidator.test { data ->
        val classifier = buildSlowClassifier(data, builders)
        var count = 0
        val f = { inputs: List<Double> ->
            count++
            if ((count and (count - 1) == 0)) {
                logger.info("Tested $count values")
            }
            classifier.predict(inputs).mostLikely()
        }
        f
    }
    */
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