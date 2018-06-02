package renegade.util

import renegade.distanceModelBuilder.*
import mu.KotlinLogging

/**
 * Created by ian on 7/9/17.
 */

private val logger = KotlinLogging.logger {}

class InputPairSampler<out InputType : Any, OutputType : Any>(
        val trainingData: List<Pair<InputType, OutputType>>, val outputDistance: (OutputType, OutputType) -> Double) {

    fun sample(maxSamples: Int)
            : InputDistances<InputType> {
        logger.info("Sampling up to $maxSamples from ${trainingData.size} training pairs")
            val randomAccessTrainingSet: List<Pair<InputType, OutputType>> =
                    if (trainingData is RandomAccess) trainingData else {
                        logger.info("trainingData is not in RandomAccess list, converting to ArrayList")
                        ArrayList(trainingData)
                    }
            val samples = randomAccessTrainingSet
                    .sampleDistinctPairs
                    .take(maxSamples)
                    .map { it: Two<Pair<InputType, OutputType>> ->
                        InputDistance(
                                Two(it.first.first, it.second.first),
                                outputDistance(it.first.second, it.second.second)
                        )
                    }.toList()
        return samples
    }
}