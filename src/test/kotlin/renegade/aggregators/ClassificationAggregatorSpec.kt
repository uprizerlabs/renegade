package renegade.aggregators

import io.kotlintest.specs.FreeSpec
import mu.KotlinLogging
import renegade.util.math.random

private val logger = KotlinLogging.logger {}

class ClassificationAggregatorSpec : FreeSpec() {
    init {
        val agg = ClassificationAggregator<Boolean>()

        val populationProp = 0.3

        val popCC = ClassificationCounter<Boolean>()

        for (x in 0 .. 1000) {
            popCC += random.nextDouble() < populationProp
        }

        logger.info("Population CC: ${popCC.toProbabilityMap()}")

        val thisProp = 0.5

        var a = agg.initialize(popCC)

        println("a on initialization: $a")

        println("population: $populationProp, this: $thisProp")

        println("x\ta\tprediction\tgain\tvariance\tvalue")
        for (x in 0 .. 100) {
            val v = random.nextDouble() < thisProp
            a = agg.aggregate(v, a)
            println("$x\t${if (v) 1 else 0}\t${agg.prediction(a)[true]}\t${agg.bias(population = popCC, of = a)}\t${agg.variance(of = a, population = popCC)}\t${agg.value(popCC, a)}")
        }
    }
}