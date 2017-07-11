package dnn.metricSpaceBuilder

import com.google.common.collect.Iterables
import dnn.util.*
import mu.KotlinLogging

/**
 * Created by ian on 7/3/17.
 */


class MetricSpaceBuilder<InputType : Any, OutputType : Any>(
        val distanceModelBuilders: DistanceModelBuilders<InputType>,
        val trainingData: List<Pair<InputType, OutputType>>,
        val maxSamples: Int = Math.min(1_000_000, Iterables.size(trainingData).sqr).toInt(),
        val outputDistance: (OutputType, OutputType) -> Double) {

    private val logger = KotlinLogging.logger {}

    fun estimateDistance(twoInputs: Two<InputType>): Double
            = this.distanceModels.estimateDistance(twoInputs)

    val distanceModels: DistanceModels<InputType> = buildRelevanceModels()

    private fun buildRelevanceModels(): DistanceModels<InputType> {
        require(distanceModelBuilders.isNotEmpty(), { "Must have at least one distanceModelBuilders regressor" })
        require(trainingData.isNotEmpty(), { "Must have at least one training instance" })

        val distancePairSampler = DistancePairSampler(trainingData, outputDistance)
        val distancePairs = distancePairSampler.sample(maxSamples)
        val distanceModels: DistanceModels<InputType> = distanceModelBuilders.buildInitial(distancePairs)
        logger.info("${distanceModels.size} distanceModelBuilders distanceModels built.")
        val refinedModels = if (distanceModelBuilders.size > 1) {
            distanceModelBuilders.refine(distanceModels, distancePairs)
        } else {
            logger.info("No refinement is required for 1 model")
            distanceModels
        }

        return refinedModels
    }
}

