package dnn.distanceModelBuilder

import dnn.metricSpaceBuilder.InputDistances
import dnn.util.*
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by ian on 7/10/17.
 */
class DistanceModelBuilderList<InputType : Any>(builders : List<DistanceModelBuilder<InputType>>)
    : List<DistanceModelBuilder<InputType>> by builders {
    private val logger = KotlinLogging.logger {}

    fun buildInitial(distancePairs: InputDistances<InputType>) =
        this.map({ modelBuilder -> modelBuilder.build(distancePairs, 1.0 / this.size) }).wrap()

    // TODO: Find a better way
    private val minRMSEImprovementProportion = 0.001

    fun refine(
            toRefine: DistanceModelList<InputType>,
            distancePairs: InputDistances<InputType>,
            iterationCounter: AtomicInteger = AtomicInteger(0)
    ): DistanceModelList<InputType> {
        checkRefineRequirements()
        var lastRMSE: Double? = null
        var firstImprovementDelta : Double? = null
        var currentModels = toRefine
        while (true) {

            val currentRMSE: Double = DistanceModel(currentModels::estimateDistance).rmse(distancePairs)

            logger.info("Refinement iteration #${iterationCounter.get()}")

            if (currentRMSE == 0.0) {
                logger.info("Current RMSE is 0.0, can't do better than that, finishing")
                break
                }

            if (lastRMSE != null) {
                val rmseImprovement = lastRMSE - currentRMSE
                if (firstImprovementDelta == null) {
                    firstImprovementDelta = rmseImprovement
                } else {
                    val minImprovementRequired = firstImprovementDelta * minRMSEImprovementProportion
                    if (rmseImprovement < minImprovementRequired) {
                        logger.info("Models are refined because last improvement ($rmseImprovement) < min required ($minImprovementRequired)")
                        break
                    }
                }
            }

            logger.mdc("current RMSE" to currentRMSE, "iteration" to iterationCounter.get().toString()) {
                currentModels = refineModelsPass(currentModels, distancePairs)
                lastRMSE = currentRMSE
            }
            iterationCounter.incrementAndGet()

        }
        return currentModels
    }

    fun refineModelsPass(modelListToRefine: DistanceModelList<InputType>, distancePairs: InputDistances<InputType>): DistanceModelList<InputType> {
        val refineOrderedByRMSE = modelListToRefine.prioritizeByRMSE(distancePairs)
        var currentModels = modelListToRefine
        for ((refiningIx, rmse) in refineOrderedByRMSE) {
            logger.mdc("model" to getLabelFor(refiningIx)) {
                val refinedModel = refineByIndex(currentModels, distancePairs, refiningIx)
                currentModels = currentModels.replace(refiningIx, refinedModel).wrap()
            }
        }
        return currentModels
    }

    private fun checkRefineRequirements() {
        require(this.size > 1, { "Refinement requires at least 2 models" })
    }


    fun refineByIndex(
            relevanceModelList: DistanceModelList<InputType>,
            relevancePairs: InputDistances<InputType>,
            toRefineIx: Int): DistanceModel<InputType> {
        logger.info("Refining ${getLabelFor(toRefineIx)}")
        val distanceDeltas = relevanceModelList.calculateDistanceDeltasExcludingModel(relevancePairs, toRefineIx)
        val refinedModel = this[toRefineIx].build(distanceDeltas)
        return refinedModel
    }

    private fun getLabelFor(ix: Int) = "$ix:${this[ix]?.label}" ?: ix

}
