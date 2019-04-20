package renegade.supervised

import mu.KotlinLogging
import renegade.MetricSpace
import renegade.aggregators.*
import renegade.aggregators.DoubleAggregator.ExtrapolatingDoubleSummary
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.indexes.MetricSpaceIndex
import renegade.indexes.waypoint.WaypointIndex
import renegade.opt.*
import renegade.util.*
import java.lang.Math.abs

private val logger = KotlinLogging.logger {}

/*
 Temporary notes for extrapolation

 http://commons.apache.org/proper/commons-math/javadocs/api-3.3/org/apache/commons/math3/stat/regression/SimpleRegression.html#getIntercept()
  */

fun <InputType: Any> Regressor(
        cfg : OptConfig,
        trainingData : List<Pair<InputType, Double>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>
) : Regressor<InputType> {

    logger.info("Building metric space")

    val metricSpace = buildMetricSpace(cfg, trainingData, distanceModelBuilders)

    dumpTopModelContributions(metricSpace)

    val msi = buildMetricSpaceIndex(cfg, trainingData, metricSpace)

    logger.info("Regressor built with configuration $cfg")
    return Regressor(cfg = cfg, index = msi)
}

private fun <InputType : Any> buildMetricSpaceIndex(cfg : OptConfig, trainingData: List<Pair<InputType, Double>>, metricSpace: MetricSpace<InputType, Double>): WaypointIndex<Pair<InputType, Double?>> {
    val msi = WaypointIndex<Pair<InputType, Double?>>(
            cfg = cfg,
            samples = trainingData,
            distance = { metricSpace.estimateDistance(Two(it.first.first, it.second.first)) }
    )
    msi.addAll(trainingData)
    return msi
}

private fun <InputType : Any> dumpTopModelContributions(metricSpace: MetricSpace<InputType, Double>) {
    metricSpace.modelContributions
            .lastEntry()
            ?.value
            ?.map { metricSpace.distanceModelList[it.key] to it.value }
            ?.sortedByDescending { it.second }
            ?.take(10)?.let { top10 ->
                logger.info("Top 10 contributing distance models:")
                logger.info("label\tscore")
                for (it in top10) {
                    logger.info("${it.first.label}\t${it.second}")
                }
            }
}

fun <InputType : Any> buildMetricSpace(cfg : OptConfig, trainingData: List<Pair<InputType, Double>>, distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>): MetricSpace<InputType, Double> {
    logger.info("Building metric space")
    val metricSpace = MetricSpace(
            cfg = cfg,
            modelBuilders = distanceModelBuilders,
            trainingData = trainingData,
            outputDistance = { a, b -> abs(a - b) }
    )
    logger.info("Metric")
    return metricSpace
}

class Regressor<InputType : Any>(
        val index: MetricSpaceIndex<Pair<InputType, Double?>, Double>, private val minimumInsetSize: Int = 100, extrapolate: Boolean = true
) {

    object Parameters {
        val extrapolate = ValueListParameter("regressor/extrapolate", true, false)
        val minimumInsetSize = ValueListParameter("regressor/minInsetSize", 5, 10, 20, 50, 100, 200)
    }

    constructor(cfg : OptConfig, index: MetricSpaceIndex<Pair<InputType, Double?>, Double>) : this(
            index,
            cfg[Parameters.minimumInsetSize],
            cfg[Parameters.extrapolate]
    )

    private val outputAggregator = DoubleAggregator(extrapolate = extrapolate)

    // FIXME: This doesn't need to be a ExtrapolatingDoubleSummary, a DoubleSummaryStatistics should be sufficient
    private val populationStats = ExtrapolatingDoubleSummary(regression = null)

    init {
        index.all().mapNotNull { it.second }.forEach {
            populationStats.addValue(ItemWithDistance(it))
        }
    }

    fun predict(input: InputType): Prediction {
        val resultSequence = index.searchFor(input to null)
        val agg = outputAggregator.initialize(null)

        data class PredictionValue(val prediction: Double, val value: Double)

        data class ValueDistance(val value : Double, val distance : Double)

        val highestValuePrediction = resultSequence
                .mapNotNull { if (it.item.second != null) ValueDistance(it.item.second!!, it.distance) else null }
                .map { outputValue ->
                    val weightedOutput = ItemWithDistance(item = outputValue.value, distance = outputValue.distance)
                    agg.addValue(weightedOutput)
                    PredictionValue(outputAggregator.prediction(agg), outputAggregator.value(populationStats, agg))
                }
                .lookAheadHighest(minimum = minimumInsetSize, valueExtractor = {it.value})
            return Prediction(
                value = highestValuePrediction!!.value.prediction,
                inSetSize = highestValuePrediction!!.index
            )

    }

    data class Prediction(val value : Double, val inSetSize : Int)
}