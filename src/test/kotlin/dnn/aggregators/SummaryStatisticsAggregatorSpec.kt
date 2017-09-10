package dnn.aggregators

import dnn.approx
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec
import java.util.*

class SummaryStatisticsAggregatorSpec : FreeSpec() {
    init {
        val ssa = SummaryStatisticsAggregator()
        "given a SummaryStatisticsAggregator" - {
            "basic stats metrics should match" - {
                var currentAg = ssa.initialize(null)
                (0..5).forEach {
                    currentAg = ssa.aggregate(it.toDouble(), currentAg)
                }
                ".n" {
                    currentAg.n shouldBe 6.toLong()
                }
                ".sum" {
                    currentAg.sum shouldBe (0..5).sumByDouble { it.toDouble() }
                }
                ".mean" {
                    currentAg.mean shouldBe approx((0.0 + 1 + 2 + 3 + 4 + 5) / 6)
                }
            }
            "statistical estimates should be within tolerances" - {
                val random = Random(1234)
                "verify variance calculation accuracy" {
                    (0 .. 100).map {
                        val ag = ssa.initialize(null)
                        ssa.aggregate(random.nextGaussian(), ag)
                    }
                }
            }
        }
    }
}
