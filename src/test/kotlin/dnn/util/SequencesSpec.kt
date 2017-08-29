package dnn.util

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

class SequencesSpec : FreeSpec() {
    init {
        "verify priority buffer" {
            val initial = ArrayList<Prioritized<Int, Double>>()
            initial += Prioritized(1, 0.5)
            initial += Prioritized(2, 0.4)
            initial += Prioritized(3, 0.3)
            val prioritized = initial.asSequence().priorityBuffer(2).toList()
            prioritized.size shouldBe 3
            prioritized[0] shouldBe Prioritized(2, 0.4)
            prioritized[1] shouldBe Prioritized(3, 0.3)
            prioritized[2] shouldBe Prioritized(1, 0.5)
        }
    }
}