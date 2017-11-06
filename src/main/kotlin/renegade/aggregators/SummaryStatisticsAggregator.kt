package renegade.aggregators

import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import renegade.util.math.sqrt

class SummaryStatisticsAggregator : OutputAggregator<Double, SummaryStatistics, Double> {
    override fun initialize(population: SummaryStatistics?): SummaryStatistics {
        val ss = SummaryStatistics()
        if (population != null) {
            val populationMean = population.mean
            (0..5).forEach { ss.addValue(populationMean) }
        }
        return ss
    }

    override fun aggregate(item: Double, summary: SummaryStatistics): SummaryStatistics {
        summary.addValue(item)
        return summary
    }

    override fun bias(population: SummaryStatistics, of: SummaryStatistics): Double {
        return Math.abs(of.mean - population.mean)
    }

    override fun variance(population: SummaryStatistics, of: SummaryStatistics): Double {
        return population.standardDeviation / of.n.toDouble().sqrt
    }

    override fun prediction(of: SummaryStatistics) = of.mean

}