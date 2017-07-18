package dnn.metricSpaceBuilder

import dnn.*
import dnn.distanceModelBuilder.InputDistances
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 7/9/17.
 */
class DistancePairSamplerSpec : FreeSpec() {
    init {
        "given a training set where the input is a double and the output is whether the input is > 50" - {
            val trainingData: List<Pair<Double, Boolean>> = (0..100).map { it.toDouble() }.map { it to (it > 50) }
            "given a DistancePairSampler appropriate to this training set" - {
                val sampler = DistancePairSampler(trainingData) {
                    a, b ->
                    if (a == b) 0.0 else 1.0
                }
                "generate 1000 relevancePairs" - {
                    val relevancePairs: InputDistances<Double> = sampler.sample(1000)
                    "the appropriate number of relevance pairs should be generated" {
                        relevancePairs.size shouldBe 1000
                    }
                    "If both are <= 50 or > 50 then relevance should be 0, otherwise it should be 1" {
                        relevancePairs.forEach { (pair, relevance) ->
                            val firstBelow50 = pair.first <= 50
                            val secondBelow50 = pair.second <= 50
                            if (firstBelow50 == secondBelow50) {
                                relevance shouldBe approx(0)
                            } else {
                                relevance shouldBe approx(1)
                            }
                        }
                    }
                }
            }
        }

    }
}