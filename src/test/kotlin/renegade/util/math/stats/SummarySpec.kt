package renegade.util.math.stats

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import renegade.util.math.sqr

class SummarySpec : FreeSpec() {
    init {
        "given values 3.7 and 2.2" - {
            val values = listOf(3.7, 2.2, 5.5).map {WeightedValue(it)}
            "obtain a Summary" - {
                val summary = values.summary()
                "count should be correct" {
                    summary.count shouldBe about(3)
                }
                val mean = values.map {it.value}.average()
                "mean should be correct" {
                    summary.mean shouldBe about(mean)
                }
                "sum should be correct"  {
                    summary.sum shouldBe about(values.map { it.value }.sum())
                }
            }
        }
        "given a set of weighted values" - {
            val values = listOf(WeightedValue(3.7, 1.0), WeightedValue(2.2, 1.0), WeightedValue(5.5, 1.0))
            val mean = values.summary().mean
            values.populationStdDev() shouldBe about(
                    Math.sqrt(values.map { WeightedValue((it.value - mean).sqr, it.weight) }.summary().mean))
            values.sampleStdDev() shouldBe about(1.65227)
        }
    }
}

private fun about(v : Number) = (v.toDouble() plusOrMinus 0.0001)