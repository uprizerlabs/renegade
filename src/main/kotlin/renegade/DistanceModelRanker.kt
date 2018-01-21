package renegade

import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import renegade.distanceModelBuilder.*

typealias Contribution = Double

class DistanceModelRanker<out InputType : Any>(private val testPairs: List<InputDistance<InputType>>) {
    fun rank(models: List<DistanceModel<InputType>>): List<IndexScore> {
        val contributions: List<ContributionsResult> = calculateContributions(models)
        val averages: List<Contribution> = contributions.asSequence().map {it.contributions}.averages()
        for ((contributions, result) in contributions) {
            val accuracy = Math.abs(contributions.sum() - result)
            for (mIx in models.indices) {
                val mIxAccuracy = Math.abs((contributions.sum() - contributions[mIx] + averages[mIx]) - result)
            }
        }
        TODO()
    }

    internal fun calculateContributions(models: List<DistanceModel<InputType>>): List<ContributionsResult> {
        return testPairs.asSequence().map { inputDistance ->
            ContributionsResult(
                    models.map { model -> model(inputDistance.inputs)},
                    inputDistance.dist
            )
        }.toList()
    }

    data class IndexScore(val index: Int, val score: Double)

    data class ContributionsResult(val contributions: List<Contribution>, val result: Double) {
        val contributionSum get() = contributions.sum()
    }
}

internal fun Sequence<List<Contribution>>.averages(): List<Contribution> {
    val stats = Array(this.first().size, { SummaryStatistics() })
    for (ar in this) {
        ar.withIndex().forEach { (ix, c) -> stats[ix].addValue(c) }
    }
    return stats.map { it.mean }
}

/*

  private fun determiningOrdering(refiner: ModelRefiner<InputType>, testInputDistances: List<InputDistance<InputType>>): List<IndexContribution> {
        val modelsAscendingByContribution = refiner.models.indices.map { modelIx ->

            val predictionErrorStats = DoubleSummaryStatistics()
            val predictionErrorWithoutModelStats = DoubleSummaryStatistics()

            for (id in testInputDistances) {
                val modelPredictions = distanceModelList.map {it.invoke(id.inputs)}
                val prediction = modelPredictions.sum()
                val predictionWithoutModel = prediction - modelPredictions[modelIx] + refiner.averageModelPrediction(modelIx)

                predictionErrorStats.accept(Math.abs(id.dist - prediction))
                predictionErrorWithoutModelStats.accept(Math.abs(id.dist - predictionWithoutModel))
            }

            val contribution = predictionErrorStats.average - predictionErrorWithoutModelStats.average
            IndexContribution(modelIx, contribution)
        }.sortedBy { it.contribution }.toList()
        return modelsAscendingByContribution
    }



 */