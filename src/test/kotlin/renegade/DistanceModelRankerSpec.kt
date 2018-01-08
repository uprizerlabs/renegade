package renegade

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

class DistanceModelRankerSpec : FreeSpec() {
    init {
        "Test internal utility methods" - {
            "averages()" {
                val averages = listOf(doubleArrayOf(0.0, 1.0), doubleArrayOf(1.0, 2.0)).asSequence().averages()
                averages.size shouldBe 2
                averages[0] shouldBe approx(listOf(0.0, 1.0).average())
                averages[1] shouldBe approx(listOf(1.0, 2.0).average())
            }
        }
    }
}