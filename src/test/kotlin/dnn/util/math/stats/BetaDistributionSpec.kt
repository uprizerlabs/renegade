package dnn.util.math.stats

import dnn.approx
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

class BetaDistributionSpec : FreeSpec() {
    init {
        "given BetaDistribution(7, 13)" - {
            val beta = BetaDistribution(7.0, 13.0)
            "verify mean" {
                beta.mean shouldBe approx(0.35)
            }
            "verify variance" {
                beta.variance shouldBe approx(13.0/1200.0)
            }
            "verify std dev" {
                beta.stddev shouldBe approx(0.104083)
            }
        }
    }
}