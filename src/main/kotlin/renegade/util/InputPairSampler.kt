package renegade.util

import mu.KotlinLogging
import renegade.distanceModelBuilder.InputDistance
import renegade.distanceModelBuilder.InputDistances
import kotlin.math.min

/**
 * Created by ian on 7/9/17.
 */

private val logger = KotlinLogging.logger {}

class InputPairSampler<out InputType : Any, OutputType : Any>(
        val trainingData: List<Pair<InputType, OutputType>>, val outputDistance: (OutputType, OutputType) -> Double) {

    fun sample(maxSamples: Int)
            : InputDistances<InputType> {
            val randomAccessTrainingSet: List<Pair<InputType, OutputType>> =
                    if (trainingData is RandomAccess) trainingData else {
                        logger.info("trainingData is not in RandomAccess list, converting to ArrayList")
                        ArrayList(trainingData)
                    }
        val sampleLimit = min(maxSamples.toLong(), randomAccessTrainingSet.size.toLong() * (randomAccessTrainingSet.size - 1)).toInt()
        logger.info("Sampling from ${trainingData.size} up to $sampleLimit pairs (maxSamples: $maxSamples)")

        val samples = randomAccessTrainingSet
                    .sampleDistinctPairs
                .take(sampleLimit)
                    .map { it: Two<Pair<InputType, OutputType>> ->
                        InputDistance(
                                Two(it.first.first, it.second.first),
                                outputDistance(it.first.second, it.second.second)
                        )
                    }.toList()
        return samples
    }
}