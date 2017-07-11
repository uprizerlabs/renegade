package dnn.metricSpaceBuilder

/**
 * Created by ian on 7/10/17.
 */
abstract class DistanceModelBuilder<in InputType : Any> {

    abstract fun build(inputDistances: InputDistances<InputType>): DistanceModel<InputType>

    fun build(
            distancePairs: InputDistances<InputType>, outputScalar: Double): DistanceModel<InputType> {
        val distanceDeltas = distancePairs.map {
            (inputs, distance) ->
            InputDistance(inputs, distance * outputScalar)
        }
        return this.build(distanceDeltas)
    }
}