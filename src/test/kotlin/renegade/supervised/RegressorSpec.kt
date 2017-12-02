package renegade.supervised

import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec
import renegade.datasets.gen.sigmoidData
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.util.math.*
import java.util.*

class RegressorSpec : FreeSpec() {
    init {
        "given a simple set of builders" - {
            val builders = ArrayList<DistanceModelBuilder<List<Double>>>()
            builders += DoubleDistanceModelBuilder().map { it[0] }
            "given a generated sigmoid curve" - {
                val data = sigmoidData()
                "and a suitable Regressor configured with no output balancing and no minimumInsetSize" - {
                    val regressor = Regressor(
                            trainingData = data,
                            distanceModelBuilders = builders,
                            minimumInsetSize = 0,
                            balanceOutput = false
                    )
                    "generate predictions" - {
                        val (rmse, bias) = calculateRMSEBias(data, regressor)
                        "verify that RMSE and bias are below thresholds" {
                            rmse should beLessThanOrEqualTo(0.013)
                            bias should beLessThanOrEqualTo(5e-4)
                        }
                    }
                }

            }
            "given a generated sigmoid curve with values > 0.5 deleted with prob 0.5, creating a deliberate imbalance" - {
                val random = Random(0)
                val data = sigmoidData().filter { it.second < 0.5 || random.nextBoolean() }
                "and given a regressor built without output balancing" - {
                    val regressorWOB = Regressor(
                            trainingData = data,
                            distanceModelBuilders = builders,
                            minimumInsetSize = 0,
                            balanceOutput = false
                    )
                    "build a regressor with output balancing" - {
                        val regressorWB = Regressor(
                                trainingData = data,
                                distanceModelBuilders = builders,
                                minimumInsetSize = 0,
                                balanceOutput = true
                        )
                        "calculate scores" {
                            println("Without")
                            val scoreWithout = calculateRMSEBias(data, regressorWOB)
                            println("With")
                            val scoreWith = calculateRMSEBias(data, regressorWB)
                            scoreWith.first shouldBe lt(scoreWithout.first)
                        }
                    }
                }
            }
        }
    }

    private fun calculateRMSEBias(data: List<Pair<List<Double>, Double>>, regressor: Regressor<List<Double>>): Pair<Double, Double> {
        var sumErrorSquared = 0.0
        var biasSum = 0.0
        println("input\tactual\tprediction")
        for (datum in data) {
            val actual = datum.second
            val prediction = regressor.predict(datum.first).value
            println("${datum.first.first()}\t$actual\t$prediction")
            sumErrorSquared += (actual - prediction).sqr
            biasSum += prediction - actual
        }
        val dataSize = data.size
        val rmse = (sumErrorSquared / dataSize).sqrt
        val bias = biasSum / dataSize
        return Pair(rmse, bias)
    }
}