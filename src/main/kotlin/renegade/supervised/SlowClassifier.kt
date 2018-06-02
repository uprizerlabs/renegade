package renegade.supervised

import renegade.aggregators.*
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.indexes.MetricSpaceIndex
import renegade.indexes.waypoint.WaypointIndex
import renegade.util.*

fun <InputType : Any, OutputType : Any> buildSlowClassifier(
        trainingData: Iterable<Pair<InputType, OutputType>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>
): SlowClassifier<InputType, OutputType> {
    val distFunc = buildDistanceFunction(distanceModelBuilders, trainingData.toList())

    fun pairDistFunc(p: Two<Pair<InputType, OutputType?>>): Double {
        return distFunc(Two(p.first.first, p.second.first))
    }

    val msi = WaypointIndex<Pair<InputType, OutputType?>>(::pairDistFunc, numWaypoints = 8, samples = trainingData)
    msi.addAll(trainingData)
    return SlowClassifier(msi)
}

class SlowClassifier<InputType : Any, OutputType : Any> internal constructor(
        val index: MetricSpaceIndex<Pair<InputType, OutputType?>, Double>
) : Classifier<InputType, OutputType> {

    private val outputAggregator = ClassificationAggregator<OutputType>()
    private val populationStats = ClassificationCounter<OutputType>()

    init {
        index.all().mapNotNull { it.second }.forEach(populationStats::plusAssign)
    }

    override fun predict(input: InputType): Map<OutputType, Double> {
        val resultSequence = index.searchFor(input to null)
        val agg = outputAggregator.initialize(populationStats)

        data class PV(val prediction: Map<OutputType, Double>, val value: Double)

        val pvLog = ArrayList<PV>()
        val highestValuePrediction = resultSequence
                .mapNotNull { it.item.second }
                .map { output ->
                    agg += output
                    PV(outputAggregator.prediction(agg), outputAggregator.value(populationStats, agg))
                }
                .map {
                    // TODO: Remove
                    pvLog += it
                    it
                }
                .lookAheadHighest(valueExtractor = { it.value })
        if (highestValuePrediction != null) {
            return highestValuePrediction.value.prediction
        } else {
            throw NullPointerException("Unable to find any results in index")
        }
    }
}

fun <OutputType> Map<OutputType, Double>.mostLikely() = this.entries.maxBy { it.value }!!.key