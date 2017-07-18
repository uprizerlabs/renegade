package dnn.distanceModelBuilder

import dnn.util.*

/**
 * Created by ian on 7/9/17.
 */

class DistanceModel<in InputType : Any>(model : (Two<InputType>) -> Double) : (Two<InputType>) -> Double by model {

    fun rmse(data: InputDistances<InputType>): Double {
        val distanceModel = this
        return Math.sqrt(data.map {
            (distanceModel(it.inputs) - it.dist).sqr
        }.average())
    }

}