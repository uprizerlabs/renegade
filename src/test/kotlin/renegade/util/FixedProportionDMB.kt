package renegade.util

import renegade.distanceModelBuilder.*
import renegade.plusAssign
import java.util.*

class FixedProportionDMB : DistanceModelBuilder<Double>(null) {
    override fun build(inputDistances: InputDistances<Double>): DistanceModel<Double> {
        val proportion = run {val proportion = DoubleSummaryStatistics()
            inputDistances.forEach { id ->
                val inputDistance = Math.abs(id.inputs.first - id.inputs.second)
                val outputDistance = id.dist
                proportion += outputDistance / inputDistance
            }
            proportion.average
        }

        return DistanceModel({inputs : Two<Double> ->
            val inputDistance = Math.abs(inputs.first - inputs.second)
            inputDistance * proportion
        })
    }

}