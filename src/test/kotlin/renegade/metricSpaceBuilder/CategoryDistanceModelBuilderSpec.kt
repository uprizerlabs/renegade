package renegade.metricSpaceBuilder

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import renegade.distanceModelBuilder.InputDistance
import renegade.distanceModelBuilder.inputTypes.CategoryDistanceModelBuilder
import renegade.util.Two
import renegade.util.math.random

/**
 * Created by ian on 7/10/17.
 */
class CategoryDistanceModelBuilderSpec : FreeSpec() {
    init {
        "given a CategoryDistanceModelBuilder" - {
            val dmb = CategoryDistanceModelBuilder()
            "given sample data for an Int input where output distance is 0 if they're within 2, 1 otherwise" - {
                val sampleData = ArrayList<InputDistance<Any>>()
                for (ix in 0..1000) {
                    val inputs = Two(random.nextInt(3), random.nextInt(3))
                    sampleData += InputDistance(inputs, if (Math.abs(inputs.first - inputs.second) < 2) 0.0 else 1.0)
                }
                "build a distance model" - {
                    var distanceModel = dmb.build(sampleData)
                    "verify output distance estimates" {
                        distanceModel.invoke(Two(0, 1)) shouldBe (0.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(1, 2)) shouldBe (0.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(0, 2)) shouldBe (1.0.plusOrMinus(0.01))
                    }
                }
            }
        }
    }
}