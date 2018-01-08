package renegade

import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import renegade.distanceModelBuilder.*

class DistanceModelRanker<out InputType : Any>(private val testPairs: List<InputDistance<InputType>>) {
    fun rank(models: List<DistanceModel<InputType>>): List<IndexScore> {
        val contributions = calculateContributions(models)
        val averages = contributions.asSequence().map {it.contributions}.averages()
        TODO()
    }

    internal fun calculateContributions(models: List<DistanceModel<InputType>>): List<ContributionsResult> {
        val contributions = testPairs.asSequence().map { inputDistance ->
            ContributionsResult(models.map { model -> model.invoke(inputDistance.inputs) }.toDoubleArray(), inputDistance.dist)
        }.toList()
        return contributions
    }

    data class IndexScore(val index: Int, val score: Double)

    data class ContributionsResult(val contributions: DoubleArray, val result: Double)
}

internal fun Sequence<DoubleArray>.averages(): DoubleArray {
    val stats = Array(this.first().size, { SummaryStatistics() })
    for (ar in this) {
        ar.withIndex().forEach { (ix, c) -> stats[ix].addValue(c) }
    }
    return stats.map { it.mean }.toDoubleArray()
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