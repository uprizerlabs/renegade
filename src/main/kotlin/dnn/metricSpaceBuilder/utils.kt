package dnn.metricSpaceBuilder

import dnn.util.Two

/**
 * Created by ian on 7/9/17.
 */

data class InputDistance<out InputType>(val inputs: Two<InputType>, val dist: Double)

typealias InputDistances<InputType> = List<InputDistance<InputType>>
