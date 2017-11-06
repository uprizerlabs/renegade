package renegade.supervised

import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec
import renegade.datasets.gen.sigmoidData
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.util.math.*

class RegressorSpec : FreeSpec() {
    init {
        "given a generaged sigmoid curve" - {
            val data = sigmoidData()
            "and a suitable Regressor" - {
                val builders = ArrayList<DistanceModelBuilder<List<Double>>>()
                builders += DoubleDistanceModelBuilder().map {it[0]}
                val regressor = Regressor(data, builders)
                "generate predictions" - {
                    var sumErrorSquared = 0.0
                    var biasSum = 0.0
                    for (datum in data) {
                        val actual = datum.second
                        val prediction = regressor.predict(datum.first)
                        sumErrorSquared += (actual-prediction).sqr
                        biasSum += prediction - actual
                    }
                    val dataSize = data.size
                    val rmse = (sumErrorSquared/ dataSize).sqrt
                    val bias = biasSum / dataSize
                    "verify that RMSE and bias are below thresholds" {
                        rmse should beLessThanOrEqualTo(0.013)
                        bias should beLessThanOrEqualTo(5e-4)
                    }
                }
            }
        }
    }
}