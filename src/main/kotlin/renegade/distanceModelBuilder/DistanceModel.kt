package renegade.distanceModelBuilder

import renegade.util.*
import renegade.util.math.sqr

/**
 * Created by ian on 7/9/17.
 */

open class DistanceModel<in InputType : Any>(model : (Two<InputType>) -> Double) : (Two<InputType>) -> Double by model {

    fun rmse(data: InputDistances<InputType>): Double {
        val distanceModel = this
        return Math.sqrt(data.map {
            (distanceModel(it.inputs) - it.dist).sqr
        }.average())
    }

}

fun <InputType : Any> List<DistanceModel<InputType>>.estimate(inputs: Two<InputType>): Double
        = this.map { it.invoke(inputs) }.sum()