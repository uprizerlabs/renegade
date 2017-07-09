package dnn.metricSpaceBuilder

import dnn.util.Two

/**
 * Created by ian on 7/9/17.
 */
class RelevancePredictor<in InputType>(val relevanceModels: Iterable<(Two<InputType>) -> Double>) {
    fun calculateRelevance(input: Two<InputType>): Double {
        val predictionSum = relevanceModels.map { relevanceModel: RelevanceModel<InputType> ->
            relevanceModel(input)
        }.sum()
        return predictionSum
    }
}