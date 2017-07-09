package dnn.metricSpaceBuilder

import dnn.util.*
import mu.KotlinLogging

/**
 * Created by ian on 7/9/17.
 */

private val logger = KotlinLogging.logger {}

internal class RelevancePairSampler<out InputType : Any, OutputType : Any>(
        val trainingSet: List<Pair<InputType, OutputType>>, val outputDistance: (OutputType, OutputType) -> Double) {
    fun sample(maxSamples: Int)
            : List<RelevanceInstance<InputType>> {
        logger.info("Building up-to $maxSamples relevance pair samples...")
        val randomAccessTrainingSet : List<Pair<InputType, OutputType>> =
                if (trainingSet is RandomAccess) trainingSet else ArrayList(trainingSet)
        return randomAccessTrainingSet
                .sampleDistinctPairs
                .take(maxSamples)
                .map { it: Two<Pair<InputType, OutputType>> ->
                    Pair(Two(it.first.first, it.second.first), outputDistance(it.first.second, it.second.second))
                }.toList()
    }
}