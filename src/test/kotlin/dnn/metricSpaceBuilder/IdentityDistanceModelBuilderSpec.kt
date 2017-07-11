package dnn.metricSpaceBuilder

import dnn.relevanceModelBuilders.IdentityDistanceModelBuilder
import dnn.util.*
import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 7/10/17.
 */
class IdentityDistanceModelBuilderSpec : FreeSpec() {
    init {
        "given an IdentityDistanceModelBuilder" - {
            val dmb = IdentityDistanceModelBuilder()
            "given sample data for an Int input where output distance is 0 if they're within 2, 1 otherwise" - {
                val sampleData = ArrayList<InputDistance<Any>>()
                for (ix in 0..1000) {
                    val inputs = Two(random.nextInt(3), random.nextInt(3))
                    sampleData += InputDistance(inputs, if (Math.abs(inputs.first - inputs.second) < 2) 0.0 else 1.0)
                }
                "build a distance model with scaled output distances" - {
                    var distanceModel = dmb.build(sampleData, 0.3)
                    "verify output distance estimates" {
                        distanceModel.invoke(Two(0, 1)) shouldBe (0.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(1, 2)) shouldBe (0.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(0, 2)) shouldBe (0.3.plusOrMinus(0.01))
                    }
                }
            }
        }
    }
}