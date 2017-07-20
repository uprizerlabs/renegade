package dnn.distanceModelBuilder

import dnn.util.abs

class PredictionCache(private val modelCount : Int, private val pairCount : Int) {
    private val contributions = Array(size = modelCount, init = {DoubleArray(size = pairCount)})
    private val absContributionTotals = DoubleArray(size = modelCount)
    private val predictions = DoubleArray(size = pairCount)

    fun updateContributions(pairIx : Int, predictionGenerator : (modelIx : Int) -> Double) {
        var sum = 0.0
        for (modelIx in 0 .. (modelCount-1)) {
            val newContribution = predictionGenerator.invoke(modelIx)
            sum += newContribution
            val prevContribution = contributions[modelIx][pairIx]
            contributions[modelIx][pairIx] = newContribution
            absContributionTotals[modelIx] += newContribution.abs - prevContribution.abs
        }
        predictions[pairIx] = sum
    }

    fun updateContribution(pairIx : Int, modelIx : Int, newPred : Double) {
        val oldPred = contributions[modelIx][pairIx]
        contributions[modelIx][pairIx] = newPred
        predictions[pairIx] += newPred - oldPred
        absContributionTotals[modelIx] += newPred.abs - oldPred.abs
    }

    fun getContribution(pairIx : Int, modelIx : Int) = contributions[modelIx][pairIx]

    fun getAbsContributionTotal(modelIx : Int) = absContributionTotals[modelIx]

    fun getPrediction(pairIx : Int) = predictions[pairIx]

    fun getPredictionWithoutContribution(pairIx : Int, modelIx : Int)
            = getPrediction(pairIx) - getContribution(pairIx, modelIx)
}