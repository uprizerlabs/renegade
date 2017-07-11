package dnn.relevanceModelBuilders

import com.github.sanity.pav.*
import com.github.sanity.pav.spline.MonotoneSpline.ExtrapolationStrategy.FLAT
import dnn.metricSpaceBuilder.*
import dnn.util.Two

open class MetricDistanceModelBuilder<in InputType : Any>(private val distanceFunction: (Two<InputType>) -> Double) : DistanceModelBuilder<InputType>() {
    override fun build(inputDistances: InputDistances<InputType>): DistanceModel<InputType> {
        val points = inputDistances.map {
            Point(distanceFunction(it.inputs), it.dist)
        }.toList()
        val pavInterpolator = PairAdjacentViolators(points).interpolator(extrapolation = FLAT)
        return DistanceModel {
            val distance = distanceFunction(it)
            pavInterpolator(distance)
        }
    }

}