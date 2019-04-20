package renegade.supervised

import mu.KotlinLogging
import renegade.aggregators.*
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.indexes.MetricSpaceIndex
import renegade.indexes.waypoint.WaypointIndex
import renegade.opt.OptConfig
import renegade.util.*
import java.io.Serializable
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

fun <InputType : Any, OutputType : Any> buildSlowClassifier(
        cfg : OptConfig,
        trainingData: Collection<Pair<InputType, OutputType>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>
): SlowClassifier<InputType, OutputType> {
    val distFunc = buildDistanceFunction(cfg, distanceModelBuilders, trainingData.toList())

    // For reasons unknown, just declaring this as an anonymous function caused a deserialization error,
    // doing it as a class fixed it.  Java serialization is not to be trusted :/
    class PairDistanceFunction : (Two<Pair<InputType, OutputType?>>) -> Double, Serializable {
        override fun invoke(p: Two<Pair<InputType, OutputType?>>)= distFunc(Two(p.first.first, p.second.first))

    }

    logger.info("Building WaypointIndex of ${trainingData.size} items")
    val msi = WaypointIndex<Pair<InputType, OutputType?>>(cfg, PairDistanceFunction(), samples = trainingData)
    msi.addAll(trainingData)
    logger.info("WaypointIndex built.")
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

        val pvLog = ConcurrentLinkedQueue<PV>()
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