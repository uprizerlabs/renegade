package dnn.relevance

import dnn.metricSpaceBuilder.*

class DoubleRelevanceRegressor : RelevanceRegressor<Double> {
    override fun invoke(p1: Iterable<RelevanceInstance<Double>>): RelevanceModel<Double> {
        TODO("turtles all the way down - use one of these regression models to estimate relevance of a number's value to itself")
    }

}