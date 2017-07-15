package dnn.distanceModelBuilder.inputTypes.metric

class DoubleDistanceModelBuilder(label : String? = null) : MetricDistanceModelBuilder<Double>(label = label, distanceFunction = { (a, b) -> Math.abs(a-b)})