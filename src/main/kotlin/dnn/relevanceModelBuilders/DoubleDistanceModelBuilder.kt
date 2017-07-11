package dnn.relevanceModelBuilders

class DoubleDistanceModelBuilder : MetricDistanceModelBuilder<Double>({ (a, b) -> Math.abs(a-b)})