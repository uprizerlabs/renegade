package renegade.distanceModelBuilder

import mu.KotlinLogging
import renegade.util.math.*

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
class ModelRefiner<InputType : Any>(
        initialModels: List<DistanceModel<InputType>>,
        private val modelBuilders: List<DistanceModelBuilder<InputType>>,
        private val pairs: InputDistances<InputType>, private val learningRate : Double = 1.0) {

    private val logger = KotlinLogging.logger {}

    private val predictions = PredictionCache(initialModels.size, pairs.size)

    private val currentModels = ArrayList(initialModels)

    init {
        pairs.withIndex().forEach { (index, pair) ->
            predictions.updateContributions(index) { modelIx ->
                initialModels[modelIx].invoke(pair.inputs)
            }
        }
    }

    val models get() = currentModels

    fun calculateRMSE(): Double
            = pairs.withIndex().map { pair ->
        (pair.value.dist - predictions.getPrediction(pair.index)).sqr
    }.average().sqrt

    fun modelTotalAvgAbsContribution(modelIx : Int) = predictions.getAbsContributionTotal(modelIx)

    fun refineModel(modelIx: Int) {
        logger.debug("Refining model $modelIx")
        val predictionsExcludingModel = predictionsExcludingModel(modelIx)
        val refinedModel = modelBuilders[modelIx].build(predictionsExcludingModel)
        updateModel(modelIx, refinedModel)
    }

    internal fun predictionsExcludingModel(modelIxToExclude: Int): InputDistances<InputType> {
        return pairs.withIndex().map { (pairIx, pair) ->
            val predictedDist = predictions.getPredictionWithoutContribution(pairIx, modelIxToExclude)
            val actualDist = pair.dist
            val delta = actualDist - predictedDist
            val previousPrediction = predictions.getContribution(pairIx, modelIxToExclude)
            val updated = (delta * learningRate) + (previousPrediction * (1.0 - learningRate))
            InputDistance(pair.inputs, updated)
        }.toList()
    }

    @Synchronized internal fun updateModel(modelIx: Int, newModel: DistanceModel<InputType>) {
        pairs
                .map { newModel.invoke(it.inputs) }
                .forEachIndexed { pairIx, newContribution -> predictions.updateContribution(pairIx, modelIx, newContribution) }
        currentModels[modelIx] = newModel
    }
}