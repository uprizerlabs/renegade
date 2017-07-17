package dnn.distanceModelBuilder

import dnn.metricSpaceBuilder.*
import dnn.util.Two
import kotlin.reflect.KProperty1

/**
 * Created by ian on 7/10/17.
 */
abstract class DistanceModelBuilder<InputType : Any>(open val label: String?) {

    abstract fun build(inputDistances: InputDistances<InputType>): DistanceModel<InputType>

    data class WeightedPriorModel<in InputType : Any>(val model : DistanceModel<InputType>, val weight: Double)

    fun build(
            distancePairs: InputDistances<InputType>,
            outputScalar: Double = 1.0,
            combineWith: WeightedPriorModel<InputType>? = null
    ): DistanceModel<InputType> {
        val distanceDeltas = distancePairs.map {
            (inputs, distance) ->
            val distanceWithPrior = combineWith.let {
                if (it == null) distance else {
                    (distance * (1.0 - it.weight) + it.model.invoke(inputs) * (it.weight))
                }
            }
            InputDistance(inputs, distanceWithPrior * outputScalar)
        }
        return this.build(distanceDeltas)
    }

    fun <SourceType : Any>
            map(mapper: (SourceType) -> InputType) = map(null, mapper)

    fun <SourceType : Any>
            map(label: String? = null,
                mapper: (SourceType) -> InputType): DistanceModelBuilder<SourceType> {

        val destTypeModelBuilder = this

        fun Two<SourceType>.map(): Two<InputType> {
            return Two(mapper(this.first), mapper(this.second))
        }

        fun mapTrainingData(sourceTrainingData: (InputDistances<SourceType>)): (InputDistances<InputType>) {
            return sourceTrainingData.map { (first, second) -> InputDistance(first.map(), second) }
        }

        val actualLabel = label ?: (mapper as? KProperty1)?.name

        val sourceTypeModelBuilder = object : DistanceModelBuilder<SourceType>(actualLabel) {
            override fun build(sourceTrainingData: InputDistances<SourceType>): DistanceModel<SourceType> {
                val mappedData: InputDistances<InputType> = mapTrainingData(sourceTrainingData)
                val destTypeModel: DistanceModel<InputType> = destTypeModelBuilder.build(mappedData)
                val sourceTypeModel = DistanceModel { twoSources: Two<SourceType> ->
                    destTypeModel.invoke(twoSources.map())
                }
                return sourceTypeModel
            }
        }
        return sourceTypeModelBuilder
    }
}