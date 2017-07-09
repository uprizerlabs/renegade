package dnn.metricSpaceBuilder

import com.google.common.collect.Iterables
import dnn.util.*
import mu.KotlinLogging

/**
 * Created by ian on 7/3/17.
 */

private val logger = KotlinLogging.logger {}

typealias RelevanceInstance<T> = Pair<Two<T>, Double>
typealias RelevanceModel<T> = (Two<T>) -> Double
typealias RelevanceRegressor<T> = (Iterable<RelevanceInstance<T>>) -> RelevanceModel<T>

class RefinedMetricSpace<InputType : Any, OutputType : Any>(
        val relevanceRegressors: List<RelevanceRegressor<InputType>>,
        val trainingSet: List<Pair<InputType, OutputType>>,
        maxSamples: Int = Math.min(1_000_000, Iterables.size(trainingSet).sqr).toInt(),
        outputDistance: (OutputType, OutputType) -> Double
) : (Two<InputType>) -> Double {

    override fun invoke(twoInputs: Two<InputType>) = estimateDistance(twoInputs, relevanceModels)

    private fun estimateDistance(twoInputs: Two<InputType>, relevanceModels : List<(Two<InputType>) -> Double>): Double = relevanceModels
            .map {
                it.invoke(twoInputs)
            }.sum()

    val relevanceModels: List<(Two<InputType>) -> Double>

    init {
        val relevancePairSampler = RelevancePairSampler(trainingSet, outputDistance)
        val relevancePairs = relevancePairSampler.sample(maxSamples)
        logger.info("Building relevance estimators")
        val estimators: MutableList<(Two<InputType>) -> Double>
                = relevanceRegressors.map({createInitial(relevancePairs, it)}).toMutableList()
        logger.info("${estimators.size} relevance estimators built.")
        if (relevanceRegressors.size > 1) {
            refineRelevanceModels(estimators, relevancePairs)
        }

        relevanceModels = estimators
    }

    private fun refineRelevanceModels(relevanceModels: MutableList<(Two<InputType>) -> Double>, relevancePairs: List<Pair<Two<InputType>, Double>>) {
        var lastRMSE: Double? = null
        var iterationCount = 0
        logger.info("refinining estimators")
        while (true) {
            logger.info("Calculating current RMSE")

            val relevancePredictor = RelevancePredictor(relevanceModels)
            val currentRMSE = rmse(relevancePairs, relevancePredictor::calculateRelevance)

            logger.info("Refinement iteration $iterationCount, RMSE: $currentRMSE")
            if (lastRMSE != null && currentRMSE >= lastRMSE) break
            val estimatorIndicesByRMSE = prioritizeRelevanceModelsByRMSE(relevanceModels, relevancePairs)

            logger.info("Priority determined: $estimatorIndicesByRMSE")
            for (refiningIx in estimatorIndicesByRMSE) {
                refineRelevanceModelByIx(relevancePairs, relevanceModels, refiningIx)
            }
            lastRMSE = currentRMSE
            iterationCount++
        }
    }

    private fun refineRelevanceModelByIx(relevancePairs: List<Pair<Two<InputType>, Double>>, relevanceModels: MutableList<(Two<InputType>) -> Double>, toRefineIx: Int) {
        logger.info("Refining $toRefineIx")
        val estimatesWithoutThisRefiner = calculateEstimatesExcludingOneRelevanceModel(relevancePairs, relevanceModels, toRefineIx)
        logger.info("Creating new refiner")
        val newRefiner = relevanceRegressors[toRefineIx].invoke(estimatesWithoutThisRefiner)
        relevanceModels[toRefineIx] = newRefiner
    }

    private fun prioritizeRelevanceModelsByRMSE(relevanceModels: MutableList<(Two<InputType>) -> Double>, relevancePairs: List<Pair<Two<InputType>, Double>>): List<Int> {
        // When refining we want to start with the estimator with the lowest RMSE as it should have the smallest
        // impact
        logger.info("Computing estimator RMSE for prioritization")
        val estimatorIndicesByRMSE = relevanceModels
                .withIndex()
                .map { (ix, estimator) ->
                    Pair(ix, rmse(relevancePairs, estimator))
                }.sortedBy { it.second }.map { it.first }
        return estimatorIndicesByRMSE
    }

    private fun calculateEstimatesExcludingOneRelevanceModel(relevancePairs: List<Pair<Two<InputType>, Double>>, relevanceModels: List<(Two<InputType>) -> Double>, modelToExcludeIx: Int): List<Pair<Two<InputType>, Double>> {
        val estimatesWithoutThisRefiner = relevancePairs.map { relevancePair ->
            val estimateWithoutThisRefiner = relevanceModels
                    .withIndex()
                    .filterNot { it.index == modelToExcludeIx }
                    .map { it.value }
                    .map {
                        it.invoke(relevancePair.first)
                    }.sum()
            Pair(relevancePair.first, estimateWithoutThisRefiner)
        }
        return estimatesWithoutThisRefiner
    }

    private fun createInitial(
            relevancePairs : List<RelevanceInstance<InputType>>,
            regressor: RelevanceRegressor<InputType>) : RelevanceModel<InputType> {
        return regressor(relevancePairs.map {
            it: Pair<Two<InputType>, Double> ->
            Pair(it.first, it.second / relevanceRegressors.size)
        })
    }

    private fun rmse(data: List<Pair<Two<InputType>, Double>>, estimator: (Two<InputType>) -> Double): Double {
        return Math.sqrt(data.map {
            (estimator(it.first) - it.second).sqr
        }.average())
    }

}

