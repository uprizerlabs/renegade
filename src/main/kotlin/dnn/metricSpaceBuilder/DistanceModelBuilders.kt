package dnn.metricSpaceBuilder

import dnn.util.*
import dnn.util.TaskResult.*
import mu.KotlinLogging

/**
 * Created by ian on 7/10/17.
 */
fun <InputType : Any> List<DistanceModelBuilder<InputType>>.wrap() = DistanceModelBuilders(this)
class DistanceModelBuilders<InputType : Any>(builders : List<DistanceModelBuilder<InputType>>)
    : List<DistanceModelBuilder<InputType>> by builders {
    private val logger = KotlinLogging.logger {}

    private val modelBuilders = this

    fun buildInitial(distancePairs: InputDistances<InputType>) =
        this.map({ modelBuilder -> modelBuilder.build(distancePairs, 1.0 / this.size) }).wrap()

    fun refine(
            toRefine: DistanceModels<InputType>,
            distancePairs: InputDistances<InputType>): DistanceModels<InputType> {
        checkRefineRequirements()
        var lastRMSE: Double?
        var iterationCount = 0
        val refinedModels = logger.infoTask("Refine modelBuilders models") {
            var currentModels = toRefine
            do {
                val currentRMSE: Double = logger.infoTask("Calculating current RMSE") {
                    val rmse = DistanceModel(currentModels::estimateDistance).rmse(distancePairs)
                    ResultWithDescription(rmse, "RMSE: $rmse")
                }

                logger.infoTask("Refinement iteration $iterationCount") {
                    currentModels = refineAllModels(currentModels, distancePairs, modelBuilders)
                    NoResult()
                }
                lastRMSE = currentRMSE
                iterationCount++
            } while (lastRMSE.let { it != null && currentRMSE < it })
            Result(currentModels)
        }
        return refinedModels
    }

    private fun refineAllModels(modelsToRefine: DistanceModels<InputType>, distancePairs: InputDistances<InputType>, modelBuilders: DistanceModelBuilders<InputType>): DistanceModels<InputType> {
        val estimatorIndicesByRMSE = modelsToRefine.prioritizeByRMSE(distancePairs)
        var currentModels = modelsToRefine
        for (refiningIx in estimatorIndicesByRMSE) {
            val builder = modelBuilders[refiningIx]
            val refinedModel = refineByIndex(modelsToRefine, distancePairs, builder, refiningIx)
            currentModels = currentModels.replace(refiningIx, refinedModel).wrap()
        }
        return currentModels
    }

    private fun checkRefineRequirements() {
        require(this.size > 1, { "Refinement requires at least 2 models" })
        require(this.size == modelBuilders.size,
                {
                    "Number of models (${this.size}) must be equal to the number of model " +
                            "builders (${modelBuilders.size})"
                }
        )
    }


    fun refineByIndex(
            relevanceModels : DistanceModels<InputType>,
            relevancePairs: InputDistances<InputType>,
            relevanceModelBuilder: DistanceModelBuilder<InputType>,
            toRefineIx: Int): DistanceModel<InputType> {
        val refinedModel = logger.infoTask("Refining distanceModelBuilders model $toRefineIx") {
            val distanceDeltas = relevanceModels.calculateDistanceDeltasExcludingModel(relevancePairs, toRefineIx)
            val refinedModel = relevanceModelBuilder.build(distanceDeltas)
            Result(refinedModel)
        }
        return refinedModel
    }
}