package renegade

import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec
import renegade.distanceModelBuilder.*
import renegade.util.Two
import renegade.util.math.random
import kotlin.math.abs

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
                data class XY(val x: Double, val y: Double) {
                    infix fun dist(o : XY) = abs(x-o.x)
                }

                val inputDistances = (0 .. 1000).map {
                    val a = XY(random.nextDouble(), random.nextDouble())
                    val b = XY(random.nextDouble(), random.nextDouble())
                    InputDistance(Two(a, b), a dist b)
                }

                val distancedModelRanker = DistanceModelRanker(inputDistances)

                val models = ArrayList<DistanceModel<XY>>()
                models += DistanceModel { (a, b) -> abs(a.x -b.x)}
                models += DistanceModel { (_, _) -> 0.0}

                val ranked = distancedModelRanker.rank(models)

                ranked[0].score shouldBe gt(0.0)
                ranked[1].score shouldBe approx(0.0)
            }
        }
    }
}