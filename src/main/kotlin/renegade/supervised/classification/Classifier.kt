package renegade.supervised.classification

import mu.KotlinLogging
import renegade.aggregators.ItemWithDistance
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.opt.OptConfig
import renegade.supervised.VertexPointLearner

private val logger = KotlinLogging.logger {}

class Classifier<InputType : Any, OutputType : Any>(
        cfg: OptConfig,
        trainingData: List<Pair<InputType, OutputType>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>
) {

    val vertexPointLearner: VertexPointLearner<InputType, OutputType, Prediction<OutputType>>

    init {
        vertexPointLearner = VertexPointLearner(
                cfg = cfg,
                trainingData = trainingData,
                distanceModelBuilders = distanceModelBuilders,
                outputDistance = { a: OutputType, b: OutputType -> if (a == b) 0.0 else 1.0 },
                predictionAggregator = this::predictionAggregator,
                predictionError = { actual: OutputType, prediction -> 1.0 - prediction[actual] }
        )
        vertexPointLearner.insetSizeOverride // TODO: This should be done in the constructor
    }

    private fun predictionAggregator(items: Collection<ItemWithDistance<OutputType>>): Prediction<OutputType> {
        val itemCount = items.size
        return Prediction(
                probabilities = items.groupingBy { it.item }
                        .eachCount()
                        .mapValues { it.value.toDouble() / itemCount },
                sampleSize = itemCount
        )
    }

    data class Prediction<OutputType>(private val probabilities: Map<OutputType, Double>, val sampleSize: Int) {
        operator fun get(output: OutputType): Double = probabilities.getOrDefault(output, 0.0)

        val mostProbable: OutputType? by lazy { probabilities.entries.maxBy { it.value }?.key }
    }

}