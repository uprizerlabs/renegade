package renegade.supervised

import io.kotlintest.specs.FreeSpec
import renegade.datasets.gen.spiral.Spiral
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder

class ClassifierSpec : FreeSpec() {
    init {
        "given generaged spiral data" - {
            val spiralData = Spiral().generate(1000)
            "and a suitable Classifier" - {
                val builders = ArrayList<DistanceModelBuilder<List<Double>>>()
                builders += DoubleDistanceModelBuilder().map {it[0]}
                builders += DoubleDistanceModelBuilder().map {it[1]}
                val classifier = Classifier(spiralData, builders)
                "generate prediction" {
                    println()
                }
            }
        }
    }
}