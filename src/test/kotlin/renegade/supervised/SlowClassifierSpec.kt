package renegade.supervised

import io.kotlintest.specs.FreeSpec
import renegade.datasets.gen.spiral.Spiral
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.opt.OptConfig

class SlowClassifierSpec : FreeSpec() {
    init {
        "given generaged spiral data" - {
            val spiralData = Spiral().generate(1000)
            "and a suitable buildSlowClassifier" - {
                val builders = ArrayList<DistanceModelBuilder<List<Double>>>()
                builders += DoubleDistanceModelBuilder().map {it[0]}
                builders += DoubleDistanceModelBuilder().map {it[1]}
                val classifier = buildSlowClassifier(OptConfig(), spiralData, builders)
                "generate prediction" {
                    println()
                }
            }
        }
    }
}