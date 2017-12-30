package renegade.datasets.mnist

import mu.KotlinLogging
import renegade.MetricSpace
import renegade.crossValidation.CorrectClassificationProportion
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.features.*
import renegade.util.toPairSequence
import java.util.zip.GZIPInputStream

/**
 * Created by ian on 7/16/17.
 */
private val logger = KotlinLogging.logger {}

val newCombinations = 100

fun main(args: Array<String>) {

    val data = loadMnistDataset("mnist_train.csv.gz").map { Pair(it.first.map { it.toDouble() }, it.second) }

    var currentFeatureTransformers: MutableList<FeatureExtractor> = ArrayList()

    for (ix in data.first().first.indices) {
        currentFeatureTransformers.add(RawFeature(ix))
    }

    while (true) {
        val builders = ArrayList<DistanceModelBuilder<List<Double>>>()
        for (featureExtractor in currentFeatureTransformers) {
            builders += DoubleDistanceModelBuilder().map(featureExtractor.toString()) { featureExtractor.invoke(it)!! }
        }

        val metricSpace = MetricSpace(builders, data, maxSamples = 100000, outputDistance = CorrectClassificationProportion())

        val featureContributions = metricSpace.modelContributions.lastEntry().value

        val bestFeatureTransformer = featureContributions.entries.maxBy { it.value }!!
        val key = bestFeatureTransformer.key
        logger.info("Top feature contribution: ${currentFeatureTransformers[key]} - ${bestFeatureTransformer.value}")

        val transformersByContribution: List<FeatureExtractor> = currentFeatureTransformers.withIndex().map { (ix, fXformer) ->
            val contribution = featureContributions.get(ix)!!
            contribution to fXformer
        }.sortedByDescending { it.first }
                .map { it.second }

        val newTransformers = transformersByContribution.take(100).toMutableList()

        transformersByContribution.asSequence().toPairSequence().take(newCombinations).forEach { combination ->
            newTransformers.addAll(generateBiops(combination.first, combination.second))
        }

        currentFeatureTransformers = newTransformers

    }

/*
    val crossValidator = CrossValidator<List<Double>, Int, Int>(SimpleSplitStrategy(0.1), CorrectClassificationProportion(), data)

    crossValidator.test { data ->
        val classifier = Classifier(data, builders)
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