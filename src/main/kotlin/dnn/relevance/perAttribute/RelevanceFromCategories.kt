package dnn.relevance.perAttribute

import com.google.common.collect.Multimap
import dnn.relevance.RelevanceMeasure
import dnn.util.*

class RelevanceFromCategories(sampling : Multimap<Two<Any>, Double>) : RelevanceMeasure<Any>(sampling) {
    // TODO: There is some inefficiency here, iterates over sampling multiple times

    private val mapping: Map<Two<Any>, AvgCount> = sampling.asMap().mapValues{ AvgCount(it.value.sum(), it.value.size) }

    override val rmse: Double
        get() = mapping.values.map {it.average to it.count.toDouble()}.weightedStdDev

    private val globalAvg = sampling.asMap().values.flatten().average()


    override fun relevance(values: Two<Any>): Double = mapping[values]?.average ?: globalAvg

    private data class AvgCount(var total : Double = 0.0, var count : Int = 0) {
        val average get() = total / count
    }

}