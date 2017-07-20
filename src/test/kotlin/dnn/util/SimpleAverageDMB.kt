package dnn.util

import dnn.distanceModelBuilder.*

class SimpleAverageDMB : DistanceModelBuilder<Double>(label = null) {
    override fun build(inputDistances: InputDistances<Double>): DistanceModel<Double> {
        val average = inputDistances.map {it.dist}.average()
        return DistanceModel {
            average
        }
    }
}