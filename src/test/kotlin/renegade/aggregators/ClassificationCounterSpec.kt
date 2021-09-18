package renegade.aggregators

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import renegade.approx

class ClassificationCounterSpec : FreeSpec() {
    init {
        "Verify classificationCounter functionality" - {
            val cc = ClassificationCounter<Int>()
            cc.toProbabilityMap().size shouldBe 0
            cc += 1
            cc += 2
            cc += 1
            ".total" {
                cc.total shouldBe approx(3.0)
            }
            ".toProbabilityMap" {
                val probMap = cc.toProbabilityMap()
                probMap.size shouldBe 2
                probMap[1] shouldBe approx(2.0 / 3.0)
                probMap[2] shouldBe approx(1.0 / 3.0)
            }
            ".toCountMap" {
                val countMap = cc.toCountMap()
                countMap.size shouldBe 2
                countMap[1] shouldBe approx(2.0)
                countMap[2] shouldBe approx(1.0)
            }
            ".classes" {
                cc.classes shouldBe setOf(1, 2)
            }
        }
    }
}