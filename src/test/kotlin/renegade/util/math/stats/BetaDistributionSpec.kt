package renegade.util.math.stats

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import renegade.approx

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