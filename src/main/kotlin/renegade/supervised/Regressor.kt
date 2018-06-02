package renegade.supervised

import mu.KotlinLogging
import renegade.MetricSpace
import renegade.aggregators.*
import renegade.aggregators.WeightedDoubleAggregator.WeightedDoubleSummary
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.indexes.MetricSpaceIndex
import renegade.indexes.waypoint.WaypointIndex
import renegade.output.UniformDoubleOutputProcessor
import renegade.util.*
import java.lang.Math.abs

private val logger = KotlinLogging.logger {}

fun <InputType: Any> Regressor(
        trainingData : List<Pair<InputType, Double>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>,
        balanceOutput: Boolean = false,
        minimumInsetSize : Int = 100
) : Regressor<InputType> {
    if (balanceOutput) {
        TODO("")
    }

    logger.info("Building metric space")
    val metricSpace = MetricSpace(
            modelBuilders = distanceModelBuilders,
            trainingData = trainingData,
            outputDistance = {a, b -> abs(a-b) }
    )
    logger.info("Building waypoint index")
    val distFunc: (Two<Pair<InputType, Double?>>) -> Double = { metricSpace.estimateDistance(Two(it.first.first, it.second.first)) }
    val msi = WaypointIndex<Pair<InputType, Double?>>(distFunc, numWaypoints = 8, samples = trainingData)
    msi.addAll(trainingData)
    logger.info("Regressor built")
    return Regressor(msi, balanceOutput, minimumInsetSize)
}

fun <InputType : Any> buildMetricSpace(trainingData: List<Pair<InputType, Double>>, distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>): MetricSpace<InputType, Double> {
    logger.info("Building metric space")
    val metricSpace = MetricSpace(
            modelBuilders = distanceModelBuilders,
            trainingData = trainingData,
            outputDistance = { a, b -> abs(a - b) }
    )
    logger.info("Metric")
    return metricSpace
}

class Regressor<InputType : Any>(
        val index: MetricSpaceIndex<Pair<InputType, Double?>, Double>, balanceOutput: Boolean = true, private val minimumInsetSize : Int = 100
) {

    private val outputAggregator = WeightedDoubleAggregator()
    private val populationStats = WeightedDoubleSummary()
    private val outputProcessor = if (balanceOutput) {
        UniformDoubleOutputProcessor(index.all().asSequence().mapNotNull { it.second })
    } else {
        null
    }

    init {
        index.all().mapNotNull { it.second }.forEach {
            val weightedValue = outputProcessor?.invoke(it) ?: Weighted(it)
            populationStats.addValue(weightedValue)}
    }

    fun predict(input: InputType): Prediction {
        val resultSequence = index.searchFor(input to null)
        val agg = outputAggregator.initialize(null)

        data class PV(val prediction : Double, val value : Double)

        val highestValuePrediction = resultSequence
                .mapNotNull { it.item.second }
                .map { outputValue ->
                    val weightedOutput = when {
                        outputProcessor != null -> outputProcessor.invoke(outputValue)
                        else -> Weighted(outputValue)
                    }
                    agg.addValue(weightedOutput)
                    PV(outputAggregator.prediction(agg), outputAggregator.value(populationStats, agg))
                }
                .lookAheadHighest(minimum = minimumInsetSize, valueExtractor = {it.value})
        return Prediction(value = highestValuePrediction!!.value.prediction, inSetSize = highestValuePrediction!!.index)
    }

    data class Prediction(val value : Double, val inSetSize : Int)
}