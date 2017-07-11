package dnn.relevanceModelBuilders

import dnn.metricSpaceBuilder.*
import dnn.util.Two

/**
 * Created by ian on 7/8/17.
 */

fun <SourceType : Any, DestType : Any>
        DistanceModelBuilder<DestType>.map(
        mapper: (SourceType) -> DestType): DistanceModelBuilder<SourceType> {

    val destTypeModelBuilder = this

    fun Two<SourceType>.map(): Two<DestType> {
        return Two(mapper(this.first), mapper(this.second))
    }

    fun mapTrainingData(sourceTrainingData: (InputDistances<SourceType>)): (InputDistances<DestType>) {
        return sourceTrainingData.map { (first, second) -> InputDistance(first.map(), second) }
    }
    val sourceTypeModelBuilder = object : DistanceModelBuilder<SourceType>() {
        override fun build(sourceTrainingData: InputDistances<SourceType>): DistanceModel<SourceType> {
            val mappedData: InputDistances<DestType> = mapTrainingData(sourceTrainingData)
            val destTypeModel: DistanceModel<DestType> = destTypeModelBuilder.build(mappedData)
            val sourceTypeModel = DistanceModel { twoSources :Two<SourceType> ->
                destTypeModel.invoke(twoSources.map())
            }
            return sourceTypeModel
        }
    }
    return sourceTypeModelBuilder
}