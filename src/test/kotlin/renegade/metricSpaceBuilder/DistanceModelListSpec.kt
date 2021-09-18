package renegade.metricSpaceBuilder

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import renegade.approx
import renegade.distanceModelBuilder.*
import renegade.util.Two
import renegade.util.math.sqr

/**
 * Created by ian on 7/9/17.
 */
class DistanceModelListSpec : FreeSpec() {
    init {
        "given two simple DistanceModelList" - {
            val difference = DistanceModel<Double>("testing") { (a, b) -> Math.abs(a - b) }
            val squaredDifference = DistanceModel<Double>("testing") { (a, b) -> (a - b).sqr }
            "create a DistanceModelList instance" - {
                val relevanceModels = listOf(difference, squaredDifference)
                "verify that estimateDistance returns the sum of the two dist models" {
                    relevanceModels.estimate(Two(5.0, 7.0)) shouldBe approx((6))
                }

            }
        }

    }
}