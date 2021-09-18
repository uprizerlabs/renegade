package renegade.distanceModelBuilder.inputTypes.metric

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import renegade.distanceModelBuilder.InputDistance
import renegade.util.*
import renegade.util.math.random

/**
 * Created by ian on 7/10/17.
 */
class DoubleDistanceModelBuilderSpec : FreeSpec() {
    init {
        "given an DoubleDistanceModelBuilder" - {
            val dmb = DoubleDistanceModelBuilder()
            "given sample data for an Int input where output distance is the input distance X 2" - {
                val sampleData = ArrayList<InputDistance<Double>>()
                for (ix in 0..1000) {
                    val inputs = Two(random.nextInt(10).toDouble(), random.nextInt(3).toDouble())
                    sampleData += InputDistance(inputs, Math.abs(inputs.first - inputs.second) * 2)
                }
                "build a distance model" - {
                    var distanceModel = dmb.build(sampleData)
                    "verify output distance estimates" {
                        distanceModel.invoke(Two(0.0, 1.0)) shouldBe (2.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(3.0, 5.0)) shouldBe (4.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(0.0, 8.0)) shouldBe (16.0.plusOrMinus(0.01))
                    }
                }
            }
        }

    }
}
