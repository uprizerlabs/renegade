package dnn.distanceModelBuilder

import dnn.approx
import dnn.util.*
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by ian on 7/12/17.
 */
class DistanceModelBuilderListSpec : FreeSpec() {

    val inputs = Two(0.0, 0.0)

    private fun simpleInputDistance(x : Double): InputDistance<Double> {
        return InputDistance(inputs, x)
    }

    init {
        "given a simple builder and initial models" - {
            val builders = DistanceModelBuilderList(listOf(SimpleAverageDMB(), SimpleAverageDMB()))
            val models = builders.buildInitial(listOf(simpleInputDistance(0.3)))
            "verify that individual builder refinement works" {
                // Each should have 0.15 output
                models[0].invoke(inputs) shouldBe approx(0.15)
                models[1].invoke(inputs) shouldBe approx(0.15)
                val refined = builders.refineByIndex(models, listOf(simpleInputDistance(0.2)), 0)
                refined.invoke(inputs) shouldBe approx(0.05)
            }
            "verify that a single refinement pass works" {
                val refined = builders.refineModelsPass(models, listOf(simpleInputDistance(0.5)))
                refined[0].invoke(inputs) shouldBe approx(0.35)
                refined[1].invoke(inputs) shouldBe approx(0.15)
            }
            "verify calculateDistanceModelContributions" {
                val distancePairs = listOf(simpleInputDistance(0.5))
                val refined = builders.refineModelsPass(models, distancePairs)
                val contributions = refined.calculateDistanceModelContributions(distancePairs)
                contributions.size shouldBe 1
                val contribution = contributions[0]
                contribution.predictions[0] shouldBe approx(0.35)
                contribution.predictions[1] shouldBe approx(0.15)
                contribution.predictionSum shouldBe approx(0.5)
                contribution.actualDistance shouldBe approx(0.5)
            }
            "verify that multi-pass refinement works" {
                val counter = AtomicInteger(0)
                val refined = builders.refine(models, listOf(simpleInputDistance(0.4)), counter)
                counter.get() shouldBe 1
                refined[0].invoke(inputs) shouldBe approx(0.25)
                refined[1].invoke(inputs) shouldBe approx(0.15)
            }
        }
    }

}