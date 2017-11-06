package renegade.supervised

import renegade.MetricSpace
import renegade.aggregators.*
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.indexes.MetricSpaceIndex
import renegade.indexes.waypoint.WaypointIndex
import renegade.util.*

fun <InputType : Any, OutputType : Any> Classifier(
        trainingData : List<Pair<InputType, OutputType>>,
        distanceModelBuilders : ArrayList<DistanceModelBuilder<InputType>>
): Classifier<InputType, OutputType> {
    val metricSpace = MetricSpace(
            modelBuilders= distanceModelBuilders,
            trainingData = trainingData,
            maxSamples = 1000,
            outputDistance = {a, b -> if (a == b) 0.0 else 1.0}
    )
    val distFunc : (Two<Pair<InputType, OutputType?>>) -> Double  = {metricSpace.estimateDistance(Two(it.first.first, it.second.first))}
    val msi = WaypointIndex<Pair<InputType, OutputType?>>(distFunc, numWaypoints = 8, samples = trainingData)
    msi.addAll(trainingData)
    return Classifier(msi)
}

class Classifier<InputType : Any, OutputType : Any>(
        val index: MetricSpaceIndex<Pair<InputType, OutputType?>, Double>
) {
    private val outputAggregator = ClassificationAggregator<OutputType>()
    private val populationStats = ClassificationCounter<OutputType>()

    init {
        index.all().mapNotNull { it.second }.forEach(populationStats::plusAssign)
    }

    fun predict(input : InputType) : Map<OutputType, Double> {
        val resultSequence = index.searchFor((input to null))
        val agg = outputAggregator.initialize(null)
        data class PV(val prediction : Map<OutputType, Double>, val value : Double)
        val highestValuePrediction = resultSequence
                .mapNotNull { it.item.second }
                .map { output ->
                    agg += output
                    PV(outputAggregator.prediction(agg), outputAggregator.value(populationStats, agg))
                }
                .lookAheadHighest(valueExtractor = {it.value})
        return highestValuePrediction!!.value.prediction
    }
}

fun <OutputType> Map<OutputType, Double>.mostLikely() = this.entries.maxBy { it.value }!!.key