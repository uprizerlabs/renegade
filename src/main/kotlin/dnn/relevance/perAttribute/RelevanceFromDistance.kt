package dnn.relevance.perAttribute

import com.github.sanity.pav.*
import com.github.sanity.pav.spline.MonotoneSpline
import com.google.common.collect.Multimap
import dnn.relevance.RelevanceMeasure
import dnn.util.*

open class RelevanceFromDistance<T>(
        data: Multimap<Two<T>, Double>,
        val distanceFunction : (v : Two<T>) -> Double
        ) : RelevanceMeasure<T>(data) {
    override val rmse: Double
        get() = pairAdjacentViolators.weightedValues.weightedStdDev

    override fun relevance(values : Two<T>): Double {
        return isotonic.invoke(distanceFunction(values))
    }

    val points = data.entries().map { Point(distanceFunction(it.key), it.value) }.asIterable()
    private val pairAdjacentViolators = PairAdjacentViolators(points, PairAdjacentViolators.PAVMode.DECREASING)
    val isotonic = pairAdjacentViolators.interpolator(extrapolation = MonotoneSpline.ExtrapolationStrategy.FLAT)
}