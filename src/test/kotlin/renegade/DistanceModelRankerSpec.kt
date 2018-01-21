package renegade

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec
import renegade.distanceModelBuilder.DistanceModel
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder

class DistanceModelRankerSpec : FreeSpec() {
    init {
        "Test internal utility methods" - {
            "averages()" {
                val averages = listOf(listOf(0.0, 1.0), listOf(1.0, 2.0)).asSequence().averages()
                averages.size shouldBe 2
                averages[0] shouldBe approx(listOf(0.0, 1.0).average())
                averages[1] shouldBe approx(listOf(1.0, 2.0).average())
            }

            "calculateContributions()" {
                val models = ArrayList<DistanceModel<Double>>()
                models += object : DistanceModel<Double>({(a, b) -> Math.abs(a-b)})
                models += object : DistanceModel<Double>({(a, b) -> Math.abs(a-b)})
                
            }
        }
    }
}