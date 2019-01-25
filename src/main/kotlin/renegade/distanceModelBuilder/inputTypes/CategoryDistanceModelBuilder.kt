package renegade.distanceModelBuilder.inputTypes

import renegade.distanceModelBuilder.*
import renegade.util.math.AveragingAccumulator

/* FIXME: even with minCount this is very likely to overfit, which we cannot allow for DMBs, so
 *        for now set to MAX_VALUE to disable cross-category records.
 *
 * Should we be using different random samplings of the distance pairs in some way that prevents
 * overfitting?
 *
 * YES - we
 *
 * OR - do we just need to limit DMBs to regression types that can't overfit, like PAV?
 */

private const val minCount = 100

class CategoryDistanceModelBuilder(override val label : String? = null) : DistanceModelBuilder<Any>(label) {

    override fun build(trainingData: InputDistances<Any>) : DistanceModel<Any> {
        require(trainingData.isNotEmpty())

        val equalityScores = trainingData
            .groupingBy { it.inputs.first == it.inputs.second }
            .fold(AveragingAccumulator(), { accumulator, element -> accumulator + element.dist })

        val pairScores = trainingData
                    .groupingBy { it.inputs }
                    .fold(AveragingAccumulator(), { accumulator, element -> accumulator + element.dist })

        val globalSum = pairScores.values.map(AveragingAccumulator::sum).sum()
        val globalCount = pairScores.values.map(AveragingAccumulator::count).sum()

        require(globalCount > 0)

        return DistanceModel(label ?: "UnlabeledCategoryDistanceModel") { pair ->
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


