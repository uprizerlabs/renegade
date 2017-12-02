package renegade.aggregators

/**
 * Created by ian on 7/15/17.
 */

interface OutputAggregator<ItemType, SummaryType, PredictionType> {

    fun initialize(population: SummaryType?): SummaryType

    fun aggregate(item: ItemType, summary: SummaryType): SummaryType

    fun bias(population: SummaryType, of: SummaryType): Double

    fun variance(population: SummaryType, of: SummaryType): Double

    fun prediction(of : SummaryType) : PredictionType

    fun value(population: SummaryType, of: SummaryType) = bias(population, of) - variance(population, of)

}

data class Weighted<ItemType>(val item : ItemType, val weight : Double = 1.0) {

}

interface WeightedOutputAggregator<ItemType, SummaryType, PredictionType>
    : OutputAggregator<Weighted<ItemType>, SummaryType, PredictionType>