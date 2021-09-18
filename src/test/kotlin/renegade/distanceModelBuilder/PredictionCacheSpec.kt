package renegade.distanceModelBuilder

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import renegade.approx

class PredictionCacheSpec : FreeSpec() {
    init {
        "verify updateContributions" {
            val cache = PredictionCache(2, 2)
            cache.updateContributions(0) { modelIx ->
                when (modelIx) {
                    0 -> 2.0
                    1 -> 3.0
                    else -> fail("Not expecting index $modelIx")
                }
            }
            cache.getContribution(pairIx = 0, modelIx = 0) shouldBe approx(2.0)
            cache.getContribution(pairIx = 0, modelIx = 1) shouldBe approx(3.0)
            cache.getPrediction(pairIx = 0) shouldBe approx(5.0)
            cache.getAbsContributionTotal(1) shouldBe approx(3.0)
            cache.getContribution(pairIx = 1, modelIx = 0) shouldBe approx(0.0)
            cache.getContribution(pairIx = 1, modelIx = 1) shouldBe approx(0.0)
            cache.getPrediction(pairIx = 1) shouldBe approx(0.0)
        }
        "verify updateContribution" {
            val cache = PredictionCache(2, 2)
            cache.getContribution(pairIx = 1, modelIx = 0) shouldBe approx(0.0)
            cache.getContribution(pairIx = 1, modelIx = 1) shouldBe approx(0.0)
            cache.getPrediction(pairIx = 1) shouldBe approx(0.0)
            cache.updateContribution(pairIx = 0, modelIx = 0, newPred = -0.6)
            cache.updateContribution(pairIx = 1, modelIx = 0, newPred = 0.4)
            cache.updateContribution(pairIx = 0, modelIx = 1, newPred = -0.2)
            cache.updateContribution(pairIx = 1, modelIx = 1, newPred = 0.7)
            cache.getContribution(pairIx = 0, modelIx = 0) shouldBe approx(-0.6)
            cache.getContribution(pairIx = 1, modelIx = 0) shouldBe approx(0.4)
            cache.getPrediction(pairIx = 1) shouldBe approx(0.4+0.7)
            cache.getAbsContributionTotal(modelIx = 0) shouldBe approx(1.0)
            cache.getAbsContributionTotal(modelIx = 1) shouldBe approx(0.9)
            cache.getAverageContribution(modelIx = 0) shouldBe approx((0.6 + 0.4) / 2.0)

        }
    }
}