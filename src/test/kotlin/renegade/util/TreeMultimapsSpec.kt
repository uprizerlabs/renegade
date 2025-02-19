package renegade.util

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import renegade.approx
import java.util.concurrent.ConcurrentSkipListMap

class TreeMultimapsSpec : FreeSpec() {
    init {
        "verify closestTo()" - {
            val mm = ConcurrentSkipListMap<Double, List<Int>>().apply {
                put(0.5, listOf(1))
                put(0.8, listOf(2))
                put(0.9, listOf(3))
                put(0.2, listOf(4))
            }

            "check number and order of returned .items for an inexact match" {
                val ret = mm.closestTo(0.81).toList()
                ret.size shouldBe 4
                ret[0].item shouldBe 2
                ret[0].distance shouldBe approx(0.81 - 0.8)
                ret[1].item shouldBe 3
                ret[1].distance shouldBe approx(0.9 - 0.81)
                ret[2].item shouldBe 1
                ret[2].distance shouldBe approx(0.81 - 0.5)
                ret[3].item shouldBe 4
                ret[3].distance shouldBe approx(0.81 - 0.2)
            }
            "check number and order of returned .items for an exact match" {
                val ret = mm.closestTo(0.8).toList()
                ret.size shouldBe 4
                ret[0].item shouldBe 2
                ret[0].distance shouldBe approx(0.0)
                ret[1].item shouldBe 3
                ret[1].distance shouldBe approx(0.9 - 0.8)
                ret[2].item shouldBe 1
                ret[2].distance shouldBe approx(0.8 - 0.5)
                ret[3].item shouldBe 4
                ret[3].distance shouldBe approx(0.8 - 0.2)
            }
        }
    }
}