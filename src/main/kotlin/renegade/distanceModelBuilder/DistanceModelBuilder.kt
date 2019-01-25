package renegade.distanceModelBuilder

import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import renegade.util.Two
import java.io.Serializable
import kotlin.reflect.KProperty1

// TODO: Make InputType Any?, and then create a DistanceModelBuilder.nullable() extension
//       method to handle nullable values

/**
 * Builds DistanceModels based on a list of [InputDistances].
 */
abstract class DistanceModelBuilder<InputType : Any?>(open val label: String?): Serializable {

    abstract fun build(inputDistances: InputDistances<InputType>): DistanceModel<InputType>

    data class WeightedPriorModel<in InputType : Any?>(val model: DistanceModel<InputType>, val weight: Double)

    fun build(
            distancePairs: InputDistances<InputType>,
            outputScalar: Double = 1.0,
            combineWith: WeightedPriorModel<InputType>? = null
    ): DistanceModel<InputType> {
        val distanceDeltas = distancePairs.map { (inputs, distance) ->
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
            map(mapper: (SourceType) -> InputType) = map(label, mapper)

    fun <SourceType : Any>
            map(mappingLabel: String? = null,
                mapper: (SourceType) -> InputType): DistanceModelBuilder<SourceType> {

        val destTypeModelBuilder = this

        fun Two<SourceType>.map(): Two<InputType> {
            return Two(mapper(this.first), mapper(this.second))
        }

        fun mapTrainingData(sourceTrainingData: (InputDistances<SourceType>)): (InputDistances<InputType>) {
            return sourceTrainingData.map { (first, second) -> InputDistance(first.map(), second) }
        }

        val propertyName = (mapper as? KProperty1)?.name
        val mapLabel = mappingLabel ?: propertyName

        val sourceTypeModelBuilder = object : DistanceModelBuilder<SourceType>(mapLabel) {
            override fun build(sourceTrainingData: InputDistances<SourceType>): DistanceModel<SourceType> {
                val mappedData: InputDistances<InputType> = mapTrainingData(sourceTrainingData)
                val destTypeModel: DistanceModel<InputType> = destTypeModelBuilder.build(mappedData)
                val sourceTypeModel = DistanceModel(mapLabel ?: "Unlabelled mappping") { twoSources: Two<SourceType> ->
                    destTypeModel.invoke(twoSources.map())
                }
                return sourceTypeModel
            }
        }
        return sourceTypeModelBuilder
    }

    fun toNullable(): DistanceModelBuilder<InputType?> {
        return object : DistanceModelBuilder<InputType?>("$label?") {
            override fun build(inputDistances: InputDistances<InputType?>): DistanceModel<InputType?> {
                val bothNullSS = SummaryStatistics()
                val oneNullSS = SummaryStatistics()
                val neitherIsNull = ArrayList<InputDistance<InputType>>()
                inputDistances.forEach {
                        when {
                            it.inputs.first == null && it.inputs.second == null -> {
                                bothNullSS.addValue(it.dist)
                            }
                            it.inputs.first != null && it.inputs.second != null -> {
                                neitherIsNull.add(InputDistance(Two(it.inputs.first!!, it.inputs.second!!), it.dist))
                            }
                            else -> oneNullSS.addValue(it.dist)

                        }
                    Unit
                }
                val bothNull = bothNullSS.mean
                val oneNull = oneNullSS.mean
                if (neitherIsNull.isNotEmpty()) {
                    val pt = this@DistanceModelBuilder.build(neitherIsNull)
                    return DistanceModel("nullable1($label)") { two ->
                        when {
                            two.first != null && two.second != null -> pt.invoke(Two(two.first!!, two.second!!))
                            two.first == null && two.second == null -> bothNull
                            else -> oneNull
                        }
                    }
                } else {
                    return DistanceModel("nullable2($label)") { two ->
                        when {
                            two.first == null && two.second == null -> bothNull
                            else -> oneNull
                        }
                    }
                }
            }
        }
    }
}