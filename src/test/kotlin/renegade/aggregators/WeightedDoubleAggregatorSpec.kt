package renegade.aggregators

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec
import renegade.approx

class WeightedDoubleAggregatorSpec : FreeSpec() {
    init {
        "given a WeightedDoubleAggregator" - {
            val wda = WeightedDoubleAggregator()
            val agg = wda.initialize(null)
            "And some weighted numbers" - {
                listOf(Weighted(0.0, 1.0), Weighted(1.0, 2.0)).forEach { agg.addValue(it) }
                "verify they are aggregated correctly" {
                    agg.count shouldBe approx(3.0)
                    agg.sum shouldBe approx(2.0)
                    agg.mean shouldBe approx(2.0 / 3.0)
                }
            }
        }
    }
}