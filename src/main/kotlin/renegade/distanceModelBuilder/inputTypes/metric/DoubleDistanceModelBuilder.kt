package renegade.distanceModelBuilder.inputTypes.metric

import renegade.util.Two
import java.io.Serializable

class DoubleDistanceModelBuilder(label : String? = null) : MetricDistanceModelBuilder<Double>(label = label, distanceFunction = AbsoluteDifferenceDistanceFunction)

private object AbsoluteDifferenceDistanceFunction : (Two<Double>) -> Double, Serializable {
    override fun invoke(pair : Two<Double>): Double {
        return Math.abs(pair.first - pair.second)
    }

}