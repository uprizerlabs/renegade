package dnn.distanceModelBuilder

import dnn.util.Two

/**
 * Created by ian on 7/8/17.
 */


fun <InputType : Any> List<DistanceModel<InputType>>.wrap() = DistanceModelList(this)

data class InputDistance<out InputType>(val inputs: Two<InputType>, val dist: Double)

typealias InputDistances<InputType> = List<InputDistance<InputType>>

fun <InputType : Any> List<DistanceModelBuilder<InputType>>.wrap() = DistanceModelBuilderList(this)
