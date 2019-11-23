package renegade.supervised

import io.kotlintest.specs.FreeSpec
import renegade.datasets.gen.sigmoidData
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.opt.OptConfig
import renegade.util.math.*
/*
class RegressorSpec : FreeSpec() {

    init {
        "given a simple set of builders" - {
            val builders = ArrayList<DistanceModelBuilder<List<Double>>>()
            builders += DoubleDistanceModelBuilder().map { it[0] }
            "given a generated sigmoid curve" - {
                val data: List<Pair<List<Double>, Double>> = sigmoidData()
                "and a suitable Regressor configured with no extrapolation" - {
                    // TODO: FIXME!
                    val regressorNoExtrap = Regressor(OptConfig(),
                            trainingData = data,
                            distanceModelBuilders = builders
                    )
                    val regressorWithExtrap = Regressor(
                            OptConfig(),
                            trainingData = data,
                            distanceModelBuilders = builders
                    )
                    "generate predictions" {

                        println("x\text\tnoext")
                        for (xL in -40..40) {
                            val x = xL.toDouble() / 10.0
                            println("$x\t${regressorWithExtrap.predict(listOf(x)).value}\t${regressorNoExtrap.predict(listOf(x)).value}")
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

 */