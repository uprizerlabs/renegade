package dnn.supervised

import dnn.MetricSpace
import dnn.aggregators.SummaryStatisticsAggregator
import dnn.distanceModelBuilder.DistanceModelBuilder
import dnn.indexes.MetricSpaceIndex
import dnn.indexes.waypoint.WaypointIndex
import dnn.util.*
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import java.lang.Math.abs

fun <InputType: Any> Regressor(trainingData : List<Pair<InputType, Double>>, distanceModelBuilders : ArrayList<DistanceModelBuilder<InputType>>) : Regressor<InputType> {
    val metricSpace = MetricSpace(modelBuilders = distanceModelBuilders, trainingData = trainingData, outputDistance = {a, b -> abs(a-b) } )
    val msi = WaypointIndex<Pair<InputType, Double?>>({metricSpace.estimateDistance(Two(it.first.first, it.second.first))}, numWaypoints = 8, samples = trainingData)
    return Regressor(msi)
}

class Regressor<InputType : Any>(
        val index: MetricSpaceIndex<Pair<InputType, Double?>, Double>
) {

    private val outputAggregator = SummaryStatisticsAggregator()
    private val populationStats = SummaryStatistics()

    init {
        index.all().mapNotNull { it.second }.forEach(populationStats::addValue)
    }

    fun predict(input: InputType): Double {
        val resultSequence = index.searchFor(input to null)
        val agg = outputAggregator.initialize(populationStats)
        data class PV(val prediction : Double, val value : Double)
        val highestValuePrediction = resultSequence
                .mapNotNull { it.item.second }
                .map { output ->
                    agg.addValue(output)
                    PV(outputAggregator.prediction(agg), outputAggregator.value(populationStats, agg))
                }
                .lookAheadHighest(valueExtractor = {it.value})
        return highestValuePrediction!!.value.prediction
    }
}