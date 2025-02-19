package renegade.distanceModelBuilder

import mu.*
import renegade.util.math.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Given a set of [modelBuilders], and a sampling of [InputDistance] [pairs], use an iterative technique to
 * create a new set of [modelBuilders] which isolates the unique contribution of each builder.
 *
 * This is similar to a dimensionality reduction technique that seeks to remove correlations between
 * input scalars, and almost university results in an improvement in discriminative ability.
 *
 * @author ian
 *
 */

private val logger = KotlinLogging.logger {}

class ModelRefiner<InputType : Any>(
        initialModels: List<DistanceModel<InputType>>,
        private val modelBuilders: List<DistanceModelBuilder<InputType>>,
        private val pairs: InputDistances<InputType>, private val learningRate : Double = 1.0) {


    private val predictionCache = PredictionCache(initialModels.size, pairs.size)

    private val currentModels = CopyOnWriteArrayList(initialModels)

    init {
        pairs.withIndex().forEach { (index, pair) ->
            predictionCache.updateContributions(index) { modelIx ->
                val result = initialModels[modelIx].invoke(pair.inputs)
                require(result.isFinite()) {"result ($result) must be finite"}
                result
            }
        }
    }

    val models get() = currentModels

    fun calculateRMSE(): Double
            = pairs.withIndex().map { pair ->
        val prediction = predictionCache.getPrediction(pair.index)
        val actual = pair.value.dist
        val v = (actual - prediction).sqr
        require(v.isFinite()) {"Non-finite value while calculating RMSE, prediction: $prediction, actual: $actual"}
        v
    }.average().sqrt

    fun modelTotalAvgAbsContribution(modelIx : Int) = predictionCache.getAbsContributionTotal(modelIx)

    fun refineModel(modelIx: Int) {
        withLoggingContext("model" to modelIx.toString()) {
            logger.debug("Refining model $modelIx")
            val predictionsExcludingModel = predictionsExcludingModel(modelIx)
            val refinedModel = modelBuilders[modelIx].build(predictionsExcludingModel)
            updateModel(modelIx, refinedModel)
        }
    }

    fun averageModelPrediction(modelIx : Int) = predictionCache.getAverageContribution(modelIx)

    internal fun predictionsExcludingModel(modelIxToExclude: Int): InputDistances<InputType> {
        return pairs.withIndex().map { (pairIx, pair) ->
            val predictedDist = predictionCache.getPredictionWithoutContribution(pairIx, modelIxToExclude)
            val actualDist = pair.dist
            val delta = actualDist - predictedDist
            val previousPrediction = predictionCache.getContribution(pairIx, modelIxToExclude)
            val updated = (delta * learningRate) + (previousPrediction * (1.0 - learningRate))
            InputDistance(pair.inputs, updated)
        }.toList()
    }

    @Synchronized internal fun updateModel(modelIx: Int, newModel: DistanceModel<InputType>) {
        pairs
                .map { newModel.invoke(it.inputs) }
                .forEachIndexed { pairIx, newContribution -> predictionCache.updateContribution(pairIx, modelIx, newContribution) }
        currentModels[modelIx] = newModel
    }
}