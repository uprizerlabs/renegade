package renegade.output

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec
import renegade.approx

class UniformDoubleOutputProcessorSpec : FreeSpec() {
    init {
        "given a UDOP" - {
            val samples = listOf(0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 2.0, 3.0, 3.0, 5.0)
            val udop = UniformDoubleOutputProcessor(samples.asSequence())
            "verify weight densities for inexact matches" - {
                "test 0.5" {
                    val weighted = udop.invoke(0.5)
                    weighted.item shouldBe approx(0.5)
                    weighted.weight shouldBe approx(0.25)
                }
                "test 1.5" {
                    val weighted = udop.invoke(1.5)
                    weighted.item shouldBe approx(1.5)
                    weighted.weight shouldBe approx(0.5)
                }
            }
            "verify weight densities for exact matches" - {
                "test 1.0" {
                    val weighted = udop.invoke(1.0)
                    weighted.item shouldBe approx(1.0)
                    weighted.weight shouldBe approx(2.0/3.0)
                }
                "test 1.5" {
                    val weighted = udop.invoke(1.5)
                    weighted.item shouldBe approx(1.5)
                    weighted.weight shouldBe approx(0.5)
                }
            }
            "verify weight densities for extreme matches" - {
                "test extreme low" {
                    val weighted = udop.invoke(-1.0)
                    weighted.item shouldBe approx(-1.0)
                    weighted.weight shouldBe approx(0.25)
                }
                "test extreme high" {
                    val weighted = udop.invoke(6.0)
                    weighted.item shouldBe approx(6.0)
                    weighted.weight shouldBe approx(1.0)
                }
            }
        }
    }
}