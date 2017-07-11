package dnn.metricSpaceBuilder

import dnn.util.*
import dnn.util.TaskResult.ResultWithDescription
import mu.KotlinLogging

/**
 * Created by ian on 7/9/17.
 */

fun <InputType : Any> List<DistanceModel<InputType>>.wrap() = DistanceModels(this)

class DistanceModels<InputType : Any>(
        modelList: List<DistanceModel<InputType>>)
    : List<DistanceModel<InputType>> by modelList {
    private val logger = KotlinLogging.logger {}

    fun prioritizeByRMSE(
            relevancePairs: InputDistances<InputType>
    ): List<Int> {
        // When refining we want to start with the estimator with the lowest RMSE as it should have the smallest
        // impact
        val relevanceModelIndicesByRMSE = logger.infoTask("Computing distanceModelBuilders model RMSE for prioritization") {
            val sortedWithIndex = this@DistanceModels
                    .withIndex()
                    .map { (ix, estimator) ->
                        Pair(ix, estimator.rmse(relevancePairs))
                    }.sortedBy { it.second }
            logger.info("Relevance model RMSEs: ${sortedWithIndex.map { "${it.first} : ${it.second}" }.joinToString(separator = ", ")}")
            val relevanceModelIndicesByRMSE = sortedWithIndex.map { it.first }
            ResultWithDescription(relevanceModelIndicesByRMSE, relevanceModelIndicesByRMSE.joinToString(separator = ", "))
        }
        return relevanceModelIndicesByRMSE
    }

    fun calculateDistanceDeltasExcludingModel(
            inputDistances: InputDistances<InputType>,
            modelToExcludeIx: Int)
            : InputDistances<InputType> {
        val otherModels = excludeModel(modelToExcludeIx)
        val estimatesWithoutThisModel = inputDistances
                .map { (inputs, distance) -> InputDistance(inputs, distance - otherModels.estimateDistance(inputs)) }
        return estimatesWithoutThisModel
    }

    fun estimateDistance(input: Two<InputType>): Double {
        val predictionSum = this.map { distanceModel: DistanceModel<InputType> ->
            distanceModel(input)
        }.sum()
        return predictionSum
    }

    private fun excludeModel(modelIndex: Int): DistanceModels<InputType> {
        return this@DistanceModels
                .asSequence()
                .withIndex()
                .filter { it.index != modelIndex }
                .map { it.value }
                .toList()
                .wrap()
    }
}