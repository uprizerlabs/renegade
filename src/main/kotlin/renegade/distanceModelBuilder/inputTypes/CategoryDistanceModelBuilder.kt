package renegade.distanceModelBuilder.inputTypes

import renegade.distanceModelBuilder.*
import renegade.util.math.AveragingAccumulator

class CategoryDistanceModelBuilder(override val label : String? = null) : DistanceModelBuilder<Any>(label) {
    private val minCount = 100

    override fun build(trainingData: InputDistances<Any>) : DistanceModel<Any> {
        val equalityScores = trainingData
            .groupingBy { it.inputs.first == it.inputs.second }
            .fold(AveragingAccumulator(), { accumulator, element -> accumulator + element.dist })
            val pairScores = trainingData
                    .groupingBy { it.inputs }
                    .fold(AveragingAccumulator(), { accumulator, element -> accumulator + element.dist })

            val globalSum = pairScores.values.map { it.sum }.sum()
            val globalCount = pairScores.values.map { it.count }.sum()

            return DistanceModel { pair ->
                val pairScore = pairScores[pair]
                if (pairScore != null && pairScore.count > minCount) pairScore.avg else {
                    val equalityScore = equalityScores[pair.first == pair.second]
                    if (equalityScore != null && equalityScore.count > minCount) {
                        equalityScore.avg
                    } else {
                        globalSum / globalCount
                    }
                }
            }
    }
}


