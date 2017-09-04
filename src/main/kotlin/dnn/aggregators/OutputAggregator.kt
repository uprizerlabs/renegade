package dnn.aggregators

/**
 * Created by ian on 7/15/17.
 */

interface OutputAggregator<ItemType, SummaryType> {

    fun initialize(population: SummaryType?): SummaryType

    fun aggregate(item: ItemType, summary: SummaryType): SummaryType

    fun bias(population: SummaryType, of: SummaryType): Double

    fun variance(population: SummaryType, of: SummaryType): Double
}

