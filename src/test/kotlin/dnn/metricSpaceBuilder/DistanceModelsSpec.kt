package dnn.metricSpaceBuilder

import dnn.approx
import dnn.util.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 7/9/17.
 */
class DistanceModelsSpec : FreeSpec() {
    init {
        "given two simple DistanceModels" - {
            val difference = DistanceModel<Double> { (a, b) -> Math.abs(a-b)}
            val squaredDifference = DistanceModel<Double> { (a, b) -> (a-b).sqr}
            "create a DistanceModels instance" - {
                val relevanceModels = listOf(difference, squaredDifference).wrap()
                "verify that estimateDistance returns the sum of the two dist models" {
                    relevanceModels.estimateDistance(Two(5.0, 7.0)) shouldBe approx((6))
                }
                val inputDistances = listOf(InputDistance(Two(5.0, 7.0), 6.0))
                val distanceDeltasExcludingModel = relevanceModels.calculateDistanceDeltasExcludingModel(inputDistances, 0)
                "verify that we can exclude a model" {
                    distanceDeltasExcludingModel.size shouldBe 1
                    distanceDeltasExcludingModel.first() shouldBe InputDistance(Two(5.0, 7.0), 2.0)
                }

            }
        }

    }
}