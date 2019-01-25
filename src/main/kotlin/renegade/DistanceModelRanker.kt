package renegade

import org.apache.commons.math3.stat.descriptive.*
import renegade.distanceModelBuilder.*
import java.util.stream.Collectors
import kotlin.math.abs

typealias Contribution = Double

class DistanceModelRanker<out InputType : Any>(private val testPairs: List<InputDistance<InputType>>) {
    fun rank(models: List<DistanceModel<InputType>>): List<IndexScore> {
        val contributionsSequence = calculateContributions(models).toList()
        val averages: List<Contribution> = contributionsSequence.map { it.contributions }.asSequence().averages()
        val modelValueStat = ArrayList<SynchronizedSummaryStatistics>()
        for (x in models.indices) {
            modelValueStat += SynchronizedSummaryStatistics()
        }
        //for ((contributions, result) in contributionsSequence) {
        contributionsSequence.parallelStream().forEach { (contributions, result) ->
            val prediction = contributions.sum()
            val accuracyLoss = Math.abs(prediction - result)
            for (leaveOutIx in models.indices) {
                val ixContribution = contributions[leaveOutIx]
                val ixAverage = averages[leaveOutIx]
                val leftOutPrediction = prediction - ixContribution + ixAverage
                val accuracyLossWithoutLeaveOutIx = abs(leftOutPrediction - result)
                val modelValue = accuracyLossWithoutLeaveOutIx - accuracyLoss
                modelValueStat[leaveOutIx].addValue(modelValue)
            }
        }
        return modelValueStat
                .withIndex()
                .map { (ix, ss) ->
                    IndexScore(ix, ss.mean)
                }.sortedByDescending { it.score }
    }

    internal fun calculateContributions(models: List<DistanceModel<InputType>>): Sequence<ContributionsResult> {
        return testPairs.parallelStream().map { inputDistance ->
            ContributionsResult(
                    models.map { model -> model(inputDistance.inputs)},
                    inputDistance.dist
            )
        }.collect(Collectors.toList()).asSequence()
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