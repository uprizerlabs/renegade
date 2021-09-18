package renegade.aggregators

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import renegade.approx
import renegade.util.math.stats.count

class DoubleAggregatorSpec : FreeSpec() {
    init {
        "given a DoubleAggregator" - {
            val wda = DoubleAggregator()
            val agg = wda.initialize(null)
            "with values 2.0 and 2.1 with distances 0.1 and 0.2 respectively" - {
                listOf(ItemWithDistance(2.0, 0.1), ItemWithDistance(2.1, 0.2)).forEach { agg.addValue(it) }
                "verify they are aggregated correctly" {
                    agg.summary.count.toDouble() shouldBe approx(2.0)
                    agg.summary.sum shouldBe approx(4.1)
                    agg.summary.mean shouldBe approx(2.05)
                }
                "verify that extrapolated value is correct" {
                    agg.estimate shouldBe approx(1.9)
                }
            }
        }
    }
}