package dnn

import com.google.common.collect.Iterables
import dnn.distanceModelBuilder.*
import dnn.metricSpaceBuilder.DistancePairSampler
import dnn.util.*
import mu.KotlinLogging

/**
 * Created by ian on 7/3/17.
 */


class MetricSpace<InputType : Any, OutputType : Any>(
        val distanceModelBuilderList: DistanceModelBuilderList<InputType>,
        val trainingData: List<Pair<InputType, OutputType>>,
        val maxSamples: Int = Math.min(1_000_000, Iterables.size(trainingData).sqr).toInt(),
        val outputDistance: (OutputType, OutputType) -> Double) : (Two<InputType>) -> Double {
    override fun invoke(inputs: Two<InputType>): Double = estimateDistance(inputs)

    private val logger = KotlinLogging.logger {}

    fun estimateDistance(twoInputs: Two<InputType>): Double
            = this.distanceModelList.estimateDistance(twoInputs)

    val distanceModelList: DistanceModelList<InputType> = buildRelevanceModels()

    private fun buildRelevanceModels(): DistanceModelList<InputType> {
        require(distanceModelBuilderList.isNotEmpty(), { "Must have at least one distanceModelBuilderList regressor" })
        require(trainingData.isNotEmpty(), { "Must have at least one training instance" })

        val distancePairSampler = DistancePairSampler(trainingData, outputDistance)
        val distancePairs = distancePairSampler.sample(maxSamples)
        val distanceModelList: DistanceModelList<InputType> = distanceModelBuilderList.buildInitial(distancePairs)
        logger.info("${distanceModelList.size} distanceModelBuilderList distanceModelList built.")
        val refinedModels = if (distanceModelBuilderList.size > 1) {
            distanceModelBuilderList.refine(distanceModelList, distancePairs)
        } else {
            logger.info("No refinement is required for 1 model")
            distanceModelList
        }

        return refinedModels
    }
}

