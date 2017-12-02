package renegade.aggregators

import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import renegade.aggregators.WeightedDoubleAggregator.WeightedDoubleSummary

class WeightedDoubleAggregator(
        private val populationPriorWeight : Double = 5.0
) : OutputAggregator<Weighted<Double>, WeightedDoubleSummary, Double> {

    private val wrapped = SummaryStatisticsAggregator()

    override fun initialize(population: WeightedDoubleSummary?): WeightedDoubleSummary {
        val w = WeightedDoubleSummary(wrapped.initialize(population?.wrappedSummaryStatistics))
        if (population != null) {
            w.addValue(Weighted(population.mean, populationPriorWeight))
        }
        return w
    }

    override fun aggregate(item: Weighted<Double>, summary: WeightedDoubleSummary): WeightedDoubleSummary {
        summary.addValue(item)
        return summary
    }

    override fun bias(population: WeightedDoubleSummary, of: WeightedDoubleSummary)
        = wrapped.bias(population.wrappedSummaryStatistics, of.wrappedSummaryStatistics)

    override fun variance(population: WeightedDoubleSummary, of: WeightedDoubleSummary)
        = wrapped.variance(population.wrappedSummaryStatistics, of.wrappedSummaryStatistics)


    override fun prediction(of: WeightedDoubleSummary)= of.mean


    class WeightedDoubleSummary(
            val wrappedSummaryStatistics : SummaryStatistics = SummaryStatistics(),
            var count : Double = 0.0,
            var sum : Double = 0.0
    ) {
        fun addValue(value : Weighted<Double>) {
            count += value.weight
            sum += value.item * value.weight
            wrappedSummaryStatistics.addValue(value.item)
        }

        val mean : Double get() {
            require(count > 0.0) {"A WeightedDoubleSummary containing no values has no mean"}
            return sum / count
        }
    }
}