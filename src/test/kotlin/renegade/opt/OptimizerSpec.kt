package renegade.opt

import io.kotest.core.spec.style.FreeSpec
import kotlin.math.abs

class OptimizerSpec : FreeSpec() {

    val paramA = IntRangeParameter("a", 0..10, 2)
    val paramB = DoubleRangeParameter("b", 0.0 to 10.0, 2.0)
    val paramC = ValueListParameter("c", "one", "two")

    fun calcScore(cfg: OptConfig): Score {
        val a = cfg[paramA]
        val b = cfg[paramB]
        val c = cfg[paramC]

        val score = Score()

        score["score"] = abs(3 - a) + abs(4.0 - b) + when (c) {
            "one" -> 4.0
            "two" -> 0.0
            else -> error("Unrecognized c: $c")
        }

        println("$cfg\t${score["score"]}")

        return score
    }

    init {
        "given an Optimizer" -
                {
                    val optimizer = Optimizer({
                        it["score"] ?: error("No 'score' key found in Score")
                    }, MemoryOptimizerLog(), { _, c -> c > 200})

                    "find optimal value" {
                        optimizer.optimize(::calcScore)
                        // TODO: Complete
                    }
                }
    }
}