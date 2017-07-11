package dnn.metricSpaceBuilder

import dnn.util.Two

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

    fun <SourceType : Any>
            map(
            mapper: (SourceType) -> InputType): DistanceModelBuilder<SourceType> {

        val destTypeModelBuilder = this

        fun Two<SourceType>.map(): Two<InputType> {
            return Two(mapper(this.first), mapper(this.second))
        }

        fun mapTrainingData(sourceTrainingData: (InputDistances<SourceType>)): (InputDistances<InputType>) {
            return sourceTrainingData.map { (first, second) -> InputDistance(first.map(), second) }
        }
        val sourceTypeModelBuilder = object : DistanceModelBuilder<SourceType>() {
            override fun build(sourceTrainingData: InputDistances<SourceType>): DistanceModel<SourceType> {
                val mappedData: InputDistances<InputType> = mapTrainingData(sourceTrainingData)
                val destTypeModel: DistanceModel<InputType> = destTypeModelBuilder.build(mappedData)
                val sourceTypeModel = DistanceModel { twoSources : Two<SourceType> ->
                    destTypeModel.invoke(twoSources.map())
                }
                return sourceTypeModel
            }
        }
        return sourceTypeModelBuilder
    }
}