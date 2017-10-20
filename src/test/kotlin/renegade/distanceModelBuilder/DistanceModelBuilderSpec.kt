package renegade.distanceModelBuilder

import renegade.distanceModelBuilder.inputTypes.IdentityDistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.util.*
import renegade.util.math.random
import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 7/11/17.
 */

class DistanceModelBuilderSpec : FreeSpec() {
    init {
        "given a DistanceModelBuilder" -{
            val dmb = DoubleDistanceModelBuilder()
            "map it so that it can parse strings instead of distances" - {
                val stringParsingDMB = dmb.map {s : String -> s.toDouble()}
                "given sample data for a string input input where output distance is the input distance X 2" - {
                    val sampleData = ArrayList<InputDistance<String>>()
                    for (ix in 0..1000) {
                        val firstInt = random.nextInt(10)
                        val secondInt = random.nextInt(10)
                        val inputs = Two(firstInt.toString(), secondInt.toString())
                        sampleData += InputDistance(inputs, Math.abs(firstInt - secondInt) * 2.0)
                    }
                    "build a distance model" - {
                        val distanceModel = stringParsingDMB.build(sampleData)
                        "verify output distance estimates" {
                            distanceModel.invoke(Two("0", "1")) shouldBe (2.0.plusOrMinus(0.01))
                            distanceModel.invoke(Two("3", "5")) shouldBe (4.0.plusOrMinus(0.01))
                            distanceModel.invoke(Two("1", "8")) shouldBe (14.0.plusOrMinus(0.01))
                        }
                    }
                }
            }
        }
        "given an IdentityDistanceModelBuilder" - {
            val dmb = IdentityDistanceModelBuilder()
            "given sample data for an Int input where output distance is 0 if they're within 2, 1 otherwise" - {
                val sampleData = ArrayList<InputDistance<Any>>()
                for (ix in 0..1000) {
                    val inputs = Two(random.nextInt(3), random.nextInt(3))
                    sampleData += InputDistance(inputs, if (Math.abs(inputs.first - inputs.second) < 2) 0.0 else 1.0)
                }
                "build a distance model with output scale" - {
                    var distanceModel = dmb.build(sampleData, 0.4)
                    "verify average default for unseen inputs adjusted by scale" {
                        distanceModel.invoke(Two(9, 9)) shouldBe ((0.2 * 0.4).plusOrMinus(0.02))
                    }
                }
            }
        }
    }

}
