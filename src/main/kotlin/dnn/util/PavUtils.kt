package dnn.util

import com.github.sanity.pav.PairAdjacentViolators

/**
 * Created by ian on 7/3/17.
 */

val PairAdjacentViolators.weightedValues get() = this.isotonicPoints.map {it.y to it.weight}
