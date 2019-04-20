package renegade

import mu.KotlinLogging
import renegade.MetricSpace.Parameters.learningRate
import renegade.MetricSpace.Parameters.maxModelCount
import renegade.MetricSpace.Parameters.maxSamples
import renegade.distanceModelBuilder.*
import renegade.opt.*
import renegade.util.*
import java.io.Serializable
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors

/**
 * Created by ian on 7/3/17.
 */

// TODO: support feature activation / deactivation and then verify that each individual feature leads to a metric improvement

private val logger = KotlinLogging.logger {}

class MetricSpace<InputType : Any, OutputType : Any>(
        val cfg : OptConfig,
        val modelBuilders: List<DistanceModelBuilder<InputType>>,
        val trainingData: List<Pair<InputType, OutputType>>, // TODO: We shouldn't have to serialize this
        val maxIterations: Int? = null,
        val outputDistance: (OutputType, OutputType) -> Double
) : (Two<InputType>) -> Double, Serializable {

    private object Parameters {
        val maxSamples = ValueListParameter("maxSamples", 1_000_000, 1_000, 10_000, 100_000, 10_000_000, 100_000_000)
        val learningRate = ValueListParameter("learningRate", 0.1, 0.01, 0.05,  0.2, 0.5, 1.0)
        val maxModelCount = ValueListParameter<Int>("maxModelCount", 64, 4, 8, 16, 32, 128, 256, 1024, 10240)
    }

    override fun invoke(inputs: Two<InputType>): Double = estimateDistance(inputs)


    fun estimateDistance(inputs: Two<InputType>): Double
            = this.distanceModelList.estimate(inputs)

    val distanceModelList: List<DistanceModel<InputType>> = buildRelevanceModels()

    // TODO: Clean up how we introspect on how the MetricSpace is built, this is ugly
    lateinit var modelContributions : TreeMap<Int, TreeMap<Int, Double>>

    private fun buildRelevanceModels(): List<DistanceModel<InputType>> {
        modelContributions = TreeMap()

        require(modelBuilders.isNotEmpty()) { "Must have at least one modelBuilders regressor" }
        require(trainingData.isNotEmpty()) { "Must have at least one training instance" }

        // TODO: Do we want to sample pairs from the training data *after* splitting?
        val distancePairs = InputPairSampler(trainingData, outputDistance).sample(cfg[maxSamples]).asSequence().splitTrainTest(2)

        distancePairs.train.map {it.dist}.average().let {averageDistance ->
            logger.info("Average distance of training distance pairs is $averageDistance")
        }

        distancePairs.test.map {it.dist}.average().let {averageDistance ->
            logger.info("Average distance of testing distance pairs is $averageDistance")
        }

        val initialDistanceModels: List<DistanceModel<InputType>> =
            (modelBuilders.parallelStream().map { modelBuilder ->
                modelBuilder.build(
                    distancePairs.train,
                    1.0 / modelBuilders.size
                )
            }.collect(Collectors.toCollection { CopyOnWriteArrayList<DistanceModel<InputType>>() }))
        logger.info("${initialDistanceModels.size} modelBuilders built.")

        return if (initialDistanceModels.size > 1) {

            val modelsDescendingByContribution = DistanceModelRanker(distancePairs.test).rank(initialDistanceModels)

            val distanceModels: List<DistanceModel<InputType>> =
                if (initialDistanceModels.size > cfg[maxModelCount]) {
                    // TODO: doing the build() twice shouldn't be necessary, but was the path of least resistance
                    // TODO: at the time of writing.
                    logger.info("Selecting best ${cfg[maxModelCount]} models and regenerating.")
                    modelsDescendingByContribution
                        .take(cfg[maxModelCount])
                        .parallelStream()
                        .map { indexScore ->
                            modelBuilders[indexScore.index].build(distancePairs.train, 1.0 / cfg[maxModelCount])
                        }.collect(Collectors.toCollection { CopyOnWriteArrayList<DistanceModel<InputType>>() })
                } else initialDistanceModels


            val refiner = ModelRefiner(distanceModels, modelBuilders, distancePairs.train, cfg[learningRate])

            val rmsesByIteration = ArrayList<Double>()

            var iteration = 0
            while (true) {
                val modelsRMSE = refiner.calculateRMSE()
                logger.info("Iteration #$iteration, RMSE: $modelsRMSE")
                rmsesByIteration += modelsRMSE
                val modelsDescendingByContribution = DistanceModelRanker(distancePairs.test).rank(refiner.models)

                val iterationLog = modelContributions.computeIfAbsent(iteration) { TreeMap() }

                for ((index, score) in modelsDescendingByContribution) {
                    iterationLog[index] = score
                    val thisIterationModelContributions = modelContributions.computeIfAbsent(iteration) { TreeMap() }
                    assert(!thisIterationModelContributions.containsKey(index)) {"thisIterationModelContributions already contains score for model $index for iteration $iteration"}
                    thisIterationModelContributions[index] = score
                }
                modelsDescendingByContribution.parallelStream().forEach { toRefine ->
                    val modelIx = toRefine.index
                    refiner.refineModel(modelIx)
                    val label = modelBuilders[modelIx].label
                    val modelName = if (label == null) modelIx.toString() else "$label:$modelIx"
                    logger.debug("Refined model #$modelName, score: ${toRefine.score}")
                }
                if (shouldTerminate(iteration, rmsesByIteration)) break
                iteration++
            }
            refiner.models
        } else {
            logger.warn("Refining skipped because we only have one model builder")
            initialDistanceModels
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
