package renegade.distanceModelBuilder

import renegade.util.Two
import renegade.util.math.sqr
import java.io.Serializable

/**
 * Takes two [InputType] values and returns a [Double] representing the distance between them.
 *
 * @author ian
 */

open class DistanceModel<in InputType : Any?>(val label : String, model : (Two<InputType>) -> Double) : (Two<InputType>) -> Double by model, Serializable {

    fun rmse(data: InputDistances<InputType>): Double {
        val distanceModel = this
        return Math.sqrt(data.map {
            (distanceModel(it.inputs) - it.dist).sqr
        }.average())
    }

}

fun <InputType : Any> List<DistanceModel<InputType>>.estimate(inputs: Two<InputType>): Double = this.map {
    val e = it.invoke(inputs)
    it.invoke(inputs)
    e
}.sum()