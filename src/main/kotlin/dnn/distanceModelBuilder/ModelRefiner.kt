package dnn.distanceModelBuilder

import dnn.util.*

class ModelRefiner<InputType : Any>(
        initialModels: DistanceModelList<InputType>,
        private val modelBuilders: DistanceModelBuilderList<InputType>,
        private val pairs: InputDistances<InputType>) {
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
        println("pair: $pair sum: ${predictions.getPrediction(pair.index)}")
        (pair.value.dist - predictions.getPrediction(pair.index)).sqr
    }.average().sqrt

    fun modelTotalAbsContribution(modelIx : Int) = predictions.getAbsContributionTotal(modelIx)

    fun refineModel(modelIx: Int) {
        val refinedModel = modelBuilders[modelIx].build(predictionsExcludingModel(modelIx))
        updateModel(modelIx, refinedModel)
    }

    internal fun predictionsExcludingModel(modelIxToExclude: Int): InputDistances<InputType> {
        return pairs.withIndex().map { (pairIx, pair) ->
            val predictedDist = predictions.getPredictionWithoutContribution(pairIx, modelIxToExclude)
            val actualDist = pair.dist
            val delta = actualDist - predictedDist
            InputDistance(pair.inputs, delta)
        }.toList()
    }

    @Synchronized internal fun updateModel(modelIx: Int, newModel: DistanceModel<InputType>) {
        pairs
                .map { newModel.invoke(it.inputs) }
                .forEachIndexed { pairIx, newContribution -> predictions.updateContribution(pairIx, modelIx, newContribution) }
        currentModels[modelIx] = newModel
    }
}