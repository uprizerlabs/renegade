package dnn.relevance

import dnn.metricSpaceBuilder.*
import dnn.util.Two

/**
 * Created by ian on 7/8/17.
 */

fun <SourceType : Any, DestType : Any>
        RelevanceRegressor<DestType>.map(
        mapper: (SourceType) -> DestType): RelevanceRegressor<SourceType> {

    val destTypeRegressor: RelevanceRegressor<DestType> = this

    fun Two<SourceType>.map(): Two<DestType> {
        return Two(mapper(this.first), mapper(this.second))
    }

    fun mapTrainingData(sourceTrainingData: (Iterable<RelevanceInstance<SourceType>>)): (Iterable<RelevanceInstance<DestType>>) {
        return sourceTrainingData.map { (first, second) -> Pair(first.map(), second) }
    }
    val sourceTypeRegressor = { sourceTrainingData : Iterable<RelevanceInstance<SourceType>> ->
        val mappedData: Iterable<RelevanceInstance<DestType>> = mapTrainingData(sourceTrainingData)
        val destTypeModel: RelevanceModel<DestType> = destTypeRegressor.invoke(mappedData)
        val sourceTypeModel: (Two<SourceType>) -> Double = { twoSources :Two<SourceType> ->
            destTypeModel.invoke(twoSources.map())
        }
        sourceTypeModel
    }
    return sourceTypeRegressor
}