package dnn.distanceModelBuilder.inputTypes.metric

import dnn.util.Two

class DoubleDistanceModelBuilder(label : String? = null) : MetricDistanceModelBuilder<Double>(label = label, distanceFunction = AbsoluteDifferenceDistanceFunction)

private object AbsoluteDifferenceDistanceFunction : (Two<Double>) -> Double {
    override fun invoke(pair : Two<Double>): Double {
        return Math.abs(pair.first - pair.second)
    }

}