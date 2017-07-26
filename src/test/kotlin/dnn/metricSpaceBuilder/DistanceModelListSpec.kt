package dnn.metricSpaceBuilder

import dnn.approx
import dnn.distanceModelBuilder.*
import dnn.util.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 7/9/17.
 */
class DistanceModelListSpec : FreeSpec() {
    init {
        "given two simple DistanceModelList" - {
            val difference = DistanceModel<Double> { (a, b) -> Math.abs(a - b) }
            val squaredDifference = DistanceModel<Double> { (a, b) -> (a - b).sqr }
            "create a DistanceModelList instance" - {
                val relevanceModels = listOf(difference, squaredDifference)
                "verify that estimateDistance returns the sum of the two dist models" {
                    relevanceModels.estimate(Two(5.0, 7.0)) shouldBe approx((6))
                }

            }
        }

    }
}