package dnn.aggregators

import dnn.util.math.stats.BetaDistribution

class ClassificationAggregator<ItemType : Any> : OutputAggregator<ItemType, ClassificationCounter<ItemType>, Map<ItemType, Double>> {
    override fun initialize(population: ClassificationCounter<ItemType>?): ClassificationCounter<ItemType> {
        return ClassificationCounter()
    }

    override fun aggregate(item: ItemType, summary: ClassificationCounter<ItemType>): ClassificationCounter<ItemType> {
        summary += item
        return summary
    }

    override fun bias(population: ClassificationCounter<ItemType>, of: ClassificationCounter<ItemType>): Double {
        val popMap = population.toProbabilityMap()
        val ofMap = of.toProbabilityMap()
        return popMap.entries.map { (k, v) ->
            Math.abs(v - ofMap.getOrElse(k, {0.0}))
        }.average()
    }

    override fun variance(population: ClassificationCounter<ItemType>, of: ClassificationCounter<ItemType>): Double {
        var countMap = of.toCountMap()
        val ttl = of.total
        return population.classes.map { cls ->
            val clsCount = countMap.getOrElse(cls, { 0 })
            BetaDistribution(clsCount + 1.0, ttl + 1.0 - clsCount).stddev
        }.average()

    }

    override fun prediction(of: ClassificationCounter<ItemType>): Map<ItemType, Double>
     = of.toProbabilityMap()

}

