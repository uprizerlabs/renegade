package dnn

import com.google.common.collect.Iterables
import dnn.distanceModelBuilder.*
import dnn.util.*
import mu.KotlinLogging

/**
 * Created by ian on 7/3/17.
 */


class MetricSpace<InputType : Any, OutputType : Any>(
        val modelBuilders: List<DistanceModelBuilder<InputType>>,
        val trainingData: List<Pair<InputType, OutputType>>,
        val maxSamples: Int = Math.min(1_000_000, Iterables.size(trainingData).sqr).toInt(),
        val learningRate : Double = 1.0,
        val outputDistance: (OutputType, OutputType) -> Double) : (Two<InputType>) -> Double {
    override fun invoke(inputs: Two<InputType>): Double = estimateDistance(inputs)

    private val logger = KotlinLogging.logger {}

    fun estimateDistance(inputs: Two<InputType>): Double
            = this.distanceModelList.estimate(inputs)

    val distanceModelList = buildRelevanceModels()

    private fun buildRelevanceModels(): List<DistanceModel<InputType>> {
        require(modelBuilders.isNotEmpty(), { "Must have at least one modelBuilders regressor" })
        require(trainingData.isNotEmpty(), { "Must have at least one training instance" })

        val distancePairs = InputPairSampler(trainingData, outputDistance).sample(maxSamples)

        distancePairs.map {it.dist}.average().let {averageDistance ->
            logger.info("Average distance of distance pairs is $averageDistance")
        }

        val distanceModelList = modelBuilders.map({ modelBuilder -> modelBuilder.build(distancePairs, 1.0 / modelBuilders.size) })
        logger.info("${distanceModelList.size} modelBuilders distanceModelList built.")

        val refiner = ModelRefiner(distanceModelList, modelBuilders, distancePairs, learningRate)

        val rmsesByIteration = ArrayList<Double>()

        var iteration = 0
        while (true) {
            val modelsRMSE = refiner.calculateRMSE()
            logger.info("Iteration #$iteration, RMSE: $modelsRMSE")
            rmsesByIteration += modelsRMSE
            val modelsAscendingByContribution = determiningOrdering(refiner)
            for (toRefine in modelsAscendingByContribution) {
                refiner.refineModel(toRefine.index)
                val contributionAfterRefinement = refiner.modelTotalAvgAbsContribution(toRefine.index)
                logger.debug("Refined model #${toRefine.index}, contribution ${toRefine.contribution} -> $contributionAfterRefinement")
            }
            if (shouldTerminate(rmsesByIteration)) break
            iteration++
        }

        return refiner.models
    }

    private fun shouldTerminate(rmses: ArrayList<Double>): Boolean {
        return if (rmses.size == 1) false else if (rmses.size > 2) {
            if (rmses.last() >= rmses[rmses.size - 2]) {
                logger.info("Terminating refinement because RMSE didn't improve")
                true
            } else {
                val initialImprovement = rmses[1] - rmses[0]
                val lastImprovement = rmses.last() - rmses[rmses.size - 2]
                if (lastImprovement < initialImprovement / 100.0) {
                    logger.info("Terminating refinement because last improvement was insignificant relative to initial improvement")
                    true
                } else {
                    false
                }
            }
        } else {
            false
        }
    }

    private fun determiningOrdering(refiner: ModelRefiner<InputType>): List<IndexContribution> {
        val modelsAscendingByContribution = refiner.models.indices.map { modelIx ->
            val contribution = refiner.modelTotalAvgAbsContribution(modelIx)
            IndexContribution(modelIx, contribution)
        }.sortedBy { it.contribution }.toList()
        return modelsAscendingByContribution
    }

    private data class IndexContribution(val index: Int, val contribution: Double)

}

