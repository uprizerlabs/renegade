package renegade.supervised

import renegade.Distance
import renegade.MetricSpace
import renegade.aggregators.ItemWithDistance
import renegade.opt.OptConfig

abstract class Learner<InputType : Any, OutputType : Any, PredictionType : Any>(
        open val cfg: OptConfig,
        open val schema : DataSchema<InputType, OutputType, PredictionType>
) {
    abstract fun learn(metric: MetricSpace<InputType, OutputType>, data: List<Pair<InputType, OutputType>>) : LearnedModel<InputType, OutputType, PredictionType>
}

interface LearnedModel<InputType : Any, OutputType : Any, PredictionType : Any> {
    fun predict(input : InputType) : PredictionType
}

/**
 * The strategy used to find a set of points in the training data to be aggregated in order to produce a prediction.
 * Depending on the strategy, this aggregation may occur at build time or during the search itself.
 */

interface PredictionStrategy<InputType : Any, OutputType : Any, PredictionType : Any> {
    fun buildSearcher() : Predictor<InputType, OutputType, PredictionType>
}

interface Predictor<InputType : Any, OutputType : Any, PredictionType : Any> {
    fun search(input : InputType) : PredictionType
}

open class DataSchema<InputType : Any, OutputType : Any, PredictionType : Any>(val outputDistance: (OutputType, OutputType) -> Distance,
                                                                           val predictionAggregator: (Collection<ItemWithDistance<OutputType>>) -> PredictionType,
                                                                           val predictionError: (OutputType, PredictionType) -> Double)