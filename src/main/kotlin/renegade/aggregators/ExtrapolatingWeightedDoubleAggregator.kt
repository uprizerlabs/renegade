package renegade.aggregators

import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import renegade.aggregators.ExtrapolatingWeightedDoubleAggregator.WeightedAggDoubleSummary

class ExtrapolatingWeightedDoubleAggregator(
        private val populationPriorWeight : Double = 5.0
) : OutputAggregator<Weighted<Double>, WeightedAggDoubleSummary, Double> {

    private val wrapped = SummaryStatisticsAggregator()

    override fun initialize(population: WeightedAggDoubleSummary?): WeightedAggDoubleSummary {
        val w = WeightedAggDoubleSummary(wrapped.initialize(population?.wrappedSummaryStatistics))
        if (population != null) {
            w.addValue(Weighted(population.mean, populationPriorWeight))
        }
        return w
    }

    override fun aggregate(item: Weighted<Double>, summary: WeightedAggDoubleSummary): WeightedAggDoubleSummary {
        summary.addValue(item)
        return summary
    }

    override fun bias(population: WeightedAggDoubleSummary, of: WeightedAggDoubleSummary)
        = wrapped.bias(population.wrappedSummaryStatistics, of.wrappedSummaryStatistics)

    override fun variance(population: WeightedAggDoubleSummary, of: WeightedAggDoubleSummary)
        = wrapped.variance(population.wrappedSummaryStatistics, of.wrappedSummaryStatistics)


    override fun prediction(of: WeightedAggDoubleSummary)= of.mean


    class WeightedAggDoubleSummary(
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
            require(count > 0.0) {"A WeightedAggDoubleSummary containing no values has no mean"}
            return sum / count
        }
    }
}