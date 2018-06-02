package renegade.supervised

import renegade.MetricSpace
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.util.Two
import java.io.Serializable

internal fun <InputType : Any, OutputType : Any> buildDistanceFunction(
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>,
        trainingData: List<Pair<InputType, OutputType>>
): (Two<InputType>) -> Double {
    val metricSpace = MetricSpace(
            modelBuilders = distanceModelBuilders,
            trainingData = trainingData,
            maxSamples = 1000000,
            outputDistance = { a, b -> if (a == b) 0.0 else 1.0 }
    )
    val distFunc: (Two<InputType>) -> Double = object : (Two<InputType>) -> Double, Serializable {
        override fun invoke(pairs: Two<InputType>): Double {
            return metricSpace.estimateDistance(Two(pairs.first, pairs.second))
        }

    }
    return distFunc
}
