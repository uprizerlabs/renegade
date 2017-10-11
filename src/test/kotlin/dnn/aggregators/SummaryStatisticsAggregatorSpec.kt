package dnn.aggregators

import dnn.approx
import dnn.util.math.random
import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import java.util.*

class SummaryStatisticsAggregatorSpec : FreeSpec() {
    init {
        val ssa = SummaryStatisticsAggregator()
        "given a SummaryStatisticsAggregator" - {
            "basic stats metrics should match" - {
                var currentAg = ssa.initialize(null)
                (0..5).forEach {
                    currentAg = ssa.aggregate(it.toDouble(), currentAg)
                }
                ".n" {
                    currentAg.n shouldBe 6.toLong()
                }
                ".sum" {
                    currentAg.sum shouldBe (0..5).sumByDouble { it.toDouble() }
                }
                ".mean" {
                    currentAg.mean shouldBe approx((0.0 + 1 + 2 + 3 + 4 + 5) / 6)
                }
            }
            "given a simple generator function where 0 = 0" - {
                fun generator(x : Double) = x
                "given some sample points drawn from the generator function" - {
                    data class ErrorStat(val predicted : SummaryStatistics = SummaryStatistics(), val actual : SummaryStatistics = SummaryStatistics()) {
                        fun addValues(p: Double, a: Double) {
                            predicted.addValue(p)
                            actual.addValue(a)
                        }
                    }
                    "compute value stats" {
                        val errorStats = TreeMap<Int, ErrorStat>()
                        for (i in 0..100) {
                            val samples = (0..50).map { random.nextGaussian() * 0.05 + it }.toList()
                            val popSA = SummaryStatistics()
                            samples.forEach { popSA.addValue(it) }
                            var sa = ssa.initialize(popSA)
                            for ((ix, sample) in samples.withIndex()) {
                                sa = ssa.aggregate(sample, sa)
                                val predictedError = ssa.value(popSA, sa)
                                val actualError = Math.abs(generator(0.0) - ssa.prediction(sa))
                                errorStats.computeIfAbsent(ix, { ErrorStat() }).addValues(predictedError, actualError)
                            }
                        }
                        val lowestErrorIndex = errorStats.entries.minBy { it.value.actual.mean }!!.key
                        val highestValueIndex = errorStats.entries.maxBy { it.value.predicted.mean }!!.key
                        Math.abs(lowestErrorIndex-highestValueIndex) should beLessThanOrEqualTo(10)

                        val dumpPredictedActual = false
                        if (dumpPredictedActual) {
                            println("index\tpredicted\tactual\tpredictedStdDev")
                            errorStats.forEach { index, stats ->
                                println("$index\t${stats.predicted.mean}\t${stats.actual.mean}\t${stats.predicted.standardDeviation}")
                            }
                        }

                    }

                }
            }
        }
    }
}
