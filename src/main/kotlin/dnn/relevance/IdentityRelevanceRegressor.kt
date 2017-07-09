package dnn.relevance

import dnn.metricSpaceBuilder.*
import dnn.util.*

class IdentityRelevanceRegressor : RelevanceRegressor<Any> {
    override fun invoke(p1: Iterable<RelevanceInstance<Any>>): RelevanceModel<Any> {
        return buildIdentityRelevanceModel(p1)
    }

    private fun buildIdentityRelevanceModel(p1: Iterable<RelevanceInstance<Any>>): (Two<Any>) -> Double {
        val pairMap = p1
                .groupingBy { it.first }
                .fold(AveragingAccumulator(), { accumulator, element -> accumulator + element.second })

        val globalSum = pairMap.values.map { it.sum }.sum()
        val globalCount = pairMap.values.map { it.count }.sum()

        return { pair -> pairMap[pair]?.avg ?: globalSum / globalCount }
    }

}