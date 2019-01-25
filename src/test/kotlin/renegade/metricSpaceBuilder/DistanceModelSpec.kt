package renegade.metricSpaceBuilder

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec
import renegade.approx
import renegade.distanceModelBuilder.*
import renegade.util.Two

/**
 * Created by ian on 7/9/17.
 */
class DistanceModelSpec : FreeSpec() {
    init {
        "Given a DistanceModel for simple Double difference" - {
            val model = DistanceModel<Double>("testing") { two -> Math.abs(two.first - two.second) }
            "With a simple dataset that exactly reflects simple double difference" - {
                val data = listOf(InputDistance(Two(1.0, 2.0), 1.0), InputDistance(Two(5.0, 7.0), 2.0))
                "RMSE should be 0" {
                    model.rmse(data) shouldBe approx(0)
                }
            }
        }

    }
}