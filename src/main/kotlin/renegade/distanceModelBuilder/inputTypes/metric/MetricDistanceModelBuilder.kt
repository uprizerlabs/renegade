package renegade.distanceModelBuilder.inputTypes.metric

import com.github.sanity.pav.*
import com.github.sanity.pav.spline.MonotoneSpline.ExtrapolationStrategy.FLAT
import renegade.distanceModelBuilder.*
import renegade.util.Two

open class MetricDistanceModelBuilder<InputType : Any>(override val label : String? = null, private val distanceFunction: (Two<InputType>) -> Double) : DistanceModelBuilder<InputType>(label) {
    override fun build(inputDistances: InputDistances<InputType>): DistanceModel<InputType> {
        val points = inputDistances.map {
            Point(distanceFunction(it.inputs), it.dist)
        }.toList()
        val pavInterpolator = PairAdjacentViolators(points).interpolator(extrapolation = FLAT)
        return DistanceModel(label ?: "Unlabeled MetricDistanceModel") {
            val distance = distanceFunction(it)
            pavInterpolator(distance)
        }
    }
}