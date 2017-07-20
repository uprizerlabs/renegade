package dnn.distanceModelBuilder

import dnn.approx
import dnn.util.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec
import java.util.*

class ModelRefinerSpec : FreeSpec() {
    init {
        "given pairs where output distance is 3X input distance" - {
            val pairs = ArrayList<InputDistance<Double>>()
            pairs += InputDistance(Two(0.0, 0.1), 0.3)
            pairs += InputDistance(Two(0.0, 0.2), 0.6)
            "and given a model builder built using these pairs and fixedprop DMBs" - {
                val modelBuilders = DistanceModelBuilderList(listOf(FixedProportionDMB(), FixedProportionDMB()).wrap())
                "build a Refiner using these initial models" - {
                    val initialModels = modelBuilders.map { it.build(pairs) }.wrap()
                    val refiner = ModelRefiner(initialModels, modelBuilders, pairs)
                    "verify that initial models are set correctly" {
                        val models = refiner.models
                        models[0].invoke(Two(0.8, 0.9)) shouldBe approx(0.3)
                        models[1].invoke(Two(0.2, 0.3)) shouldBe approx(0.3)
                    }
                    "verify that models can be refined successfully" {
                        refiner.refineModel(0)
                        refiner.models[0].invoke(Two(0.0, 0.1)) shouldBe approx(0.0)
                        refiner.refineModel(1)
                        refiner.models[1].invoke(Two(0.0, 0.1)) shouldBe approx(0.3)

                        refiner.calculateRMSE() shouldBe approx(0.0)
                    }
                }
            }
        }
    }
}