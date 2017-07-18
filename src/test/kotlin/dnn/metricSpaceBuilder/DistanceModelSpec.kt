package dnn.metricSpaceBuilder

import dnn.approx
import dnn.distanceModelBuilder.*
import dnn.util.Two
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 7/9/17.
 */
class DistanceModelSpec : FreeSpec() {
    init {
        "Given a DistanceModel for simple Double difference" - {
            val model = DistanceModel<Double> { two -> Math.abs(two.first - two.second) }
            "With a simple dataset that exactly reflects simple double difference" - {
                val data = listOf(InputDistance(Two(1.0, 2.0), 1.0), InputDistance(Two(5.0, 7.0), 2.0))
                "RMSE should be 0" {
                    model.rmse(data) shouldBe approx(0)
                }
            }
        }

    }
}