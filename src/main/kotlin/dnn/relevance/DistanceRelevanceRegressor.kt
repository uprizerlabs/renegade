package dnn.relevance

import com.github.sanity.pav.*
import com.github.sanity.pav.spline.MonotoneSpline.ExtrapolationStrategy.FLAT
import dnn.metricSpaceBuilder.RelevanceRegressor
import dnn.util.Two

class DistanceRelevanceRegressor<in T>(private val distanceFunction: (Two<T>) -> Double) : RelevanceRegressor<T> {
    override fun invoke(p1: Iterable<Pair<Two<T>, Double>>): (Two<T>) -> Double {
        val points = p1.map {
            Point(distanceFunction(it.first), it.second)
        }.toList()
        val pavInterpolator = PairAdjacentViolators(points).interpolator(extrapolation = FLAT)
        return {
            val distance = distanceFunction(it)
            pavInterpolator(distance)
        }
    }
}