package renegade.util

import com.google.common.collect.TreeMultimap
import renegade.approx
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

class TreeMultimapsSpec : FreeSpec() {
    init {
        "verify closestTo()" - {
            val mm = TreeMultimap.create<Double, Int>().apply {
                put(0.5, 1)
                put(0.8, 2)
                put(0.9, 3)
                put(0.2, 4)
            }
            "check number and order of returned values for an inexact match" {
                val ret = mm.closestTo(0.81).toList()
                ret.size shouldBe 4
                ret[0].first shouldBe 2
                ret[0].second shouldBe approx(0.81 - 0.8)
                ret[1].first shouldBe 3
                ret[1].second shouldBe approx(0.9 - 0.81)
                ret[2].first shouldBe 1
                ret[2].second shouldBe approx(0.81 - 0.5)
                ret[3].first shouldBe 4
                ret[3].second shouldBe approx(0.81 - 0.2)
            }
            "check number and order of returned values for an exact match" {
                val ret = mm.closestTo(0.8).toList()
                ret.size shouldBe 4
                ret[0].first shouldBe 2
                ret[0].second shouldBe approx(0.0)
                ret[1].first shouldBe 3
                ret[1].second shouldBe approx(0.9 - 0.8)
                ret[2].first shouldBe 1
                ret[2].second shouldBe approx(0.8 - 0.5)
                ret[3].first shouldBe 4
                ret[3].second shouldBe approx(0.8 - 0.2)
            }
        }
    }
}