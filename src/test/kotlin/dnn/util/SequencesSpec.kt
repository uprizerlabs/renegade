package dnn.util

import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec
import java.util.HashSet
import kotlin.collections.ArrayList

class SequencesSpec : FreeSpec() {
    init {
        "given a priorized list of 3 points" - {
            val initial = ArrayList<Prioritized<Int, Double>>()
            initial += Prioritized(1, 0.5)
            initial += Prioritized(2, 0.4)
            initial += Prioritized(3, 0.3)
            "verify that priorityBuffer orders them as expected" {
                val prioritized = initial.asSequence().priorityBuffer(2).toList()
                prioritized.size shouldBe 3
                prioritized[0] shouldBe Prioritized(2, 0.4)
                prioritized[1] shouldBe Prioritized(3, 0.3)
                prioritized[2] shouldBe Prioritized(1, 0.5)
            }
        }

        "given a sequence 1, 2, 3, 4" - {
            val sequence = listOf(1, 2, 3, 4)
            "exhaustively generate all pairs" - {
                val exhaustivePairSet = HashSet<Pair<Int, Int>>()
                for (a in sequence) {
                    for (b in sequence) {
                        if (a != b) {
                            exhaustivePairSet += (a to b)
                        }
                    }
                }
                "generate a pairSequence" - {
                    val pairs = sequence.asSequence().toPairSequence().toList()
                    "Verify that there is the correct number of pairs" {
                        pairs.size shouldBe exhaustivePairSet.size
                    }
                    "Verify that all pairs were generated, and no superfluous pairs were generated" {
                        exhaustivePairSet.intersect(pairs).size shouldBe exhaustivePairSet.size
                    }
                    "Verify that the pairs were generated in the appropriate order" {
                        var p : Pair<Int, Int>? = null
                        for (pair in pairs) {
                            if (p != null) {
                                (p.first + p.second) shouldBe beLessThanOrEqualTo(pair.first + pair.second)
                            }
                            p = pair
                        }
                    }
                }
            }
        }

    }
}