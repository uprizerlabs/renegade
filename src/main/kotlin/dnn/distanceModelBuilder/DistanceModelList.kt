package dnn.distanceModelBuilder

import dnn.util.Two
import mu.KotlinLogging

/**
 * Created by ian on 7/9/17.
 */


class DistanceModelList<InputType : Any>(
        modelList: List<DistanceModel<InputType>>)
    : List<DistanceModel<InputType>> by modelList {
    private val logger = KotlinLogging.logger {}

    fun prioritizeByRMSE(
            relevancePairs: InputDistances<InputType>
    ): LinkedHashMap<Int, Double> {
        // When refining we want to start with the estimator with the worst RMSE as it's likely to have the
        // smallest impact
        // TODO: Should we just look at the average delta?
        val sortedWithIndex = this@DistanceModelList
                    .withIndex()
                    .map { (ix, estimator) ->
                        Pair(ix, estimator.rmse(relevancePairs))
                    }.sortedByDescending { it.second }
        return sortedWithIndex.map { it.first to it.second }.toMap(LinkedHashMap())
    }

    class ModelPredictions(val predictions : DoubleArray, val predictionSum : Double, val actualDistance : Double)

    fun calculateDistanceModelContributions(inputDistances: InputDistances<InputType>) : List<ModelPredictions> {
        return inputDistances.map {inputDistance ->
            val predictions = DoubleArray(size = this.size)
            var predictionSum = 0.0
            this.withIndex().forEach { (ix, distanceModel) ->
                val prediction = distanceModel.invoke(inputDistance.inputs)
                predictions[ix] = prediction
                predictionSum += prediction
            }
            ModelPredictions(predictions, predictionSum, inputDistance.dist)
        }
    }

    fun calculateDistanceDeltasExcludingModel(
            inputDistances: InputDistances<InputType>,
            modelToExcludeIx: Int)
            : InputDistances<InputType> {
        val otherModels = excludeModel(modelToExcludeIx)
        val estimatesWithoutThisModel = inputDistances
                .map { (inputs, distance) ->
                    InputDistance(inputs, distance - otherModels.estimateDistance(inputs))
                }
        return estimatesWithoutThisModel
    }

    fun estimateDistance(input: Two<InputType>): Double {
        val predictionSum = this.map { distanceModel: DistanceModel<InputType> ->
            distanceModel(input)
        }.sum()
        return predictionSum
    }

    private fun excludeModel(modelIndex: Int): DistanceModelList<InputType> {
        return this@DistanceModelList
                .asSequence()
                .withIndex()
                .filter { it.index != modelIndex }
                .map { it.value }
                .toList()
                .wrap()
    }
}