package renegade

import com.google.common.collect.Iterables
import mu.KotlinLogging
import renegade.distanceModelBuilder.*
import renegade.util.*
import renegade.util.math.sqr
import java.util.*

/**
 * Created by ian on 7/3/17.
 */

// TODO: support feature activation / deactivation and then verify that each individual feature leads to a metric improvement

class MetricSpace<InputType : Any, OutputType : Any>(
        val modelBuilders: List<DistanceModelBuilder<InputType>>,
        val trainingData: List<Pair<InputType, OutputType>>,
        val maxSamples: Int = Math.min(1_000_000, Iterables.size(trainingData).sqr).toInt(),
        val learningRate: Double = 0.1, val maxIterations: Int? = null,
        val outputDistance: (OutputType, OutputType) -> Double) : (Two<InputType>) -> Double {
    override fun invoke(inputs: Two<InputType>): Double = estimateDistance(inputs)

    private val logger = KotlinLogging.logger {}

    fun estimateDistance(inputs: Two<InputType>): Double
            = this.distanceModelList.estimate(inputs)

    val distanceModelList: List<DistanceModel<InputType>> = buildRelevanceModels()

    // TODO: Clean up how we introspect on how the MetricSpace is built, this is ugly
    lateinit var modelContributions : TreeMap<Int, TreeMap<Int, Double>>

    private fun buildRelevanceModels(): List<DistanceModel<InputType>> {
        modelContributions = TreeMap()

        require(modelBuilders.isNotEmpty(), { "Must have at least one modelBuilders regressor" })
        require(trainingData.isNotEmpty(), { "Must have at least one training instance" })

        val distancePairs = InputPairSampler(trainingData, outputDistance).sample(maxSamples).asSequence().splitTrainTest(2)

        distancePairs.train.map {it.dist}.average().let {averageDistance ->
            logger.info("Average distance of training distance pairs is $averageDistance")
        }

        distancePairs.test.map {it.dist}.average().let {averageDistance ->
            logger.info("Average distance of testing distance pairs is $averageDistance")
        }


        val distanceModelList = modelBuilders.map({ modelBuilder -> modelBuilder.build(distancePairs.train, 1.0 / modelBuilders.size) })
        logger.info("${distanceModelList.size} modelBuilders built.")

        return if (distanceModelList.size > 1) {
            val refiner = ModelRefiner(distanceModelList, modelBuilders, distancePairs.train, learningRate)

            val rmsesByIteration = ArrayList<Double>()

            var iteration = 0
            while (true) {
                val modelsRMSE = refiner.calculateRMSE()
                logger.info("Iteration #$iteration, RMSE: $modelsRMSE")
                rmsesByIteration += modelsRMSE
                val modelsAscendingByContribution = determiningOrdering(refiner, distancePairs.test)
                for ((index, contribution) in modelsAscendingByContribution) {
                    val modelContributions = modelContributions.computeIfAbsent(iteration, { TreeMap() })
                    assert(!modelContributions.containsKey(index))
                    modelContributions.put(index, contribution)
                }
                for (toRefine in modelsAscendingByContribution) {
                    val modelIx = toRefine.index
                    refiner.refineModel(modelIx)
                    val contributionAfterRefinement = refiner.modelTotalAvgAbsContribution(modelIx)
                    val label = modelBuilders[modelIx].label
                    val modelName = if (label == null) modelIx.toString() else "$label:$modelIx"
                    logger.info("Refined model #$modelName, contribution ${toRefine.contribution} -> $contributionAfterRefinement")
                }
                if (shouldTerminate(iteration, rmsesByIteration)) break
                iteration++
            }
            refiner.models
        } else {
            logger.warn("Refining skipped because we only have one model builder")
            distanceModelList
        }

    }

    private fun shouldTerminate(iteration: Int, rmses: ArrayList<Double>): Boolean {
        return if (maxIterations != null && iteration == maxIterations) {
            logger.info("Terminating refinement because we've reached maxIterations")
            true
        } else {
            if (rmses.size == 1) false else if (rmses.size > 2) {
                if (rmses.last() >= rmses[rmses.size - 2]) {
                    logger.info("Terminating refinement because RMSE didn't improve")
                    true
                } else {
                    val initialImprovement = rmses[0] - rmses[1]
                    val lastImprovement = rmses[rmses.size - 2] - rmses[rmses.size - 1]
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
    }


}
