package renegade.distanceModelBuilder

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import renegade.distanceModelBuilder.inputTypes.CategoryDistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.util.Two
import renegade.util.math.random

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
            "create nullable version" - {
                val ndmb = dmb.toNullable()
                val sampleData = ArrayList<InputDistance<Double?>>()
                for (ix in 0..1000) {
                    val firstInt = random.nextInt(10).toDouble()
                    val secondInt = random.nextInt(10).toDouble()
                    val inputs = Two(firstInt, secondInt)
                    sampleData += InputDistance(inputs, Math.abs(firstInt - secondInt) * 2.0)
                }
                sampleData += InputDistance(Two(null, null), 31.0)
                sampleData += InputDistance(Two(1.0, null), 25.0)

                "build a distance model" - {
                    val distanceModel = ndmb.build(sampleData)
                    "verify output distance estimates" {
                        distanceModel.invoke(Two(0.0, 1.0)) shouldBe (2.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(3.0, 5.0)) shouldBe (4.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(1.0, 8.0)) shouldBe (14.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(null, null)) shouldBe (31.0.plusOrMinus(0.01))
                        distanceModel.invoke(Two(1.0, null)) shouldBe (25.0.plusOrMinus(0.01))

                    }
                }
            }
        }
        "given an CategoryDistanceModelBuilder" - {
            val dmb = CategoryDistanceModelBuilder()
            "given sample data for an Int input where output distance is 0 if they're within 2, 1 otherwise" - {
                val sampleData = ArrayList<InputDistance<Any>>()
                for (ix in 0..1000) {
                    val inputs = Two(random.nextInt(3), random.nextInt(3))
                    sampleData += InputDistance(inputs, if (Math.abs(inputs.first - inputs.second) < 2) 0.0 else 1.0)
                }
                // Disabled because CategoryDistanceModelBuilder was modified to only recognize equality versus
                // inequality.
                /*
                "build a distance model with output scale" - {
                    var distanceModel = dmb.build(sampleData, 0.4)
                    "verify average default for unseen inputs adjusted by scale" {
                        distanceModel.invoke(Two(9, 9)) shouldBe ((0.2 * 0.4).plusOrMinus(0.02))
                    }
                }
                */
            }
        }
    }

}
