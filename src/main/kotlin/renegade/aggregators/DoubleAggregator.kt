package renegade.aggregators

import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import org.apache.commons.math3.stat.regression.SimpleRegression
import renegade.aggregators.DoubleAggregator.ExtrapolatingDoubleSummary

class DoubleAggregator(
        private val populationPriorWeight: Double = 5.0, private val extrapolate: Boolean = true
) : OutputAggregator<ItemWithDistance<Double>, ExtrapolatingDoubleSummary, Double> {

    // TODO: Is this still needed?
    private val wrapped = SummaryStatisticsAggregator()

    override fun initialize(population: ExtrapolatingDoubleSummary?): ExtrapolatingDoubleSummary {
        val w = ExtrapolatingDoubleSummary(wrapped.initialize(population?.summary), if (extrapolate) SimpleRegression() else null)
        if (population != null) {
            w.addValue(ItemWithDistance(population.summary.mean, populationPriorWeight))
        }
        return w
    }

    override fun aggregate(item: ItemWithDistance<Double>, summary: ExtrapolatingDoubleSummary): ExtrapolatingDoubleSummary {
        summary.addValue(item)
        return summary
    }

    override fun bias(population: ExtrapolatingDoubleSummary, of: ExtrapolatingDoubleSummary)
            = wrapped.bias(population.summary, of.summary)

    override fun variance(population: ExtrapolatingDoubleSummary, of: ExtrapolatingDoubleSummary)
            = wrapped.variance(population.summary, of.summary)

    override fun prediction(of: ExtrapolatingDoubleSummary) = of.estimate

    class ExtrapolatingDoubleSummary(
            val summary: SummaryStatistics = SummaryStatistics(), val regression: SimpleRegression?
    ) {

        fun addValue(value: ItemWithDistance<Double>) {
            summary.addValue(value.item)
            regression?.addData(value.distance, value.item)
        }

        val estimate: Double
            get() {
                require(summary.n > 0.0) { "A ExtrapolatingDoubleSummary containing no values has no mean" }
                val intercept = regression?.intercept
                return if (intercept != null && intercept.isFinite()) intercept else summary.mean
            }
    }
}