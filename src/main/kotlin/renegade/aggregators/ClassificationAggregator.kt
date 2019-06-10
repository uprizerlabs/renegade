package renegade.aggregators

import renegade.util.math.stats.BetaDistribution
import java.io.Serializable
import kotlin.math.round

private const val POPULATION_WEIGHT = 5.0

class ClassificationAggregator<ItemType : Any> : OutputAggregator<ItemType, ClassificationCounter<ItemType>, Map<ItemType, Double>>, Serializable {
    override fun initialize(population: ClassificationCounter<ItemType>?): ClassificationCounter<ItemType> {
        val cc = ClassificationCounter<ItemType>()
        if (population != null) {
            val pm = population.toProbabilityMap()
            val minProb  = pm.values.min() ?: throw RuntimeException("population is empty")
            pm.forEach { item, count -> cc.addWithCount(item, count * POPULATION_WEIGHT / minProb) }
        }
        return cc
    }

    override fun aggregate(item: ItemType, summary: ClassificationCounter<ItemType>): ClassificationCounter<ItemType> {
        summary += item
        return summary
    }

    override fun bias(population: ClassificationCounter<ItemType>, of: ClassificationCounter<ItemType>): Double {
       // val minThreshold = round(1.0 / population.toProbabilityMap().map { it.value }.min()!!) * 10
      //  if (of.total < minThreshold) return 0.0

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
            val clsCount = countMap.getOrElse(cls, { 0.0 })
            BetaDistribution(clsCount + 1.0, ttl + 1.0 - clsCount).stddev
        }.average()

    }

    override fun prediction(of: ClassificationCounter<ItemType>): Map<ItemType, Double>
     = of.toProbabilityMap()

}

