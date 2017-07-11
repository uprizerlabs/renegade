package dnn.metricSpaceBuilder

import dnn.util.*
import dnn.util.TaskResult.ResultWithDescription
import mu.KotlinLogging

/**
 * Created by ian on 7/9/17.
 */


internal class DistancePairSampler<out InputType : Any, OutputType : Any>(
        val trainingData: List<Pair<InputType, OutputType>>, val outputDistance: (OutputType, OutputType) -> Double) {
    private val logger = KotlinLogging.logger {}

    fun sample(maxSamples: Int)
            : InputDistances<InputType> {
        val samples = logger.infoTask("Sampling up to $maxSamples from ${trainingData.size} training pairs") {
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
            ResultWithDescription(samples, "${samples.size} samples")
        }
        return samples
    }
}