package renegade.output

import renegade.aggregators.Weighted
import java.util.*
import kotlin.collections.MutableMap.MutableEntry

/**
 * Reweights an output value such that output values have a uniform distribution.
 */
class UniformDoubleOutputProcessor(samples: Sequence<Double>) : OutputProcessor<Double>() {

    private val valueCounts = samples.groupingBy { it }.eachCountTo(TreeMap())

    val lowestKnownValue = valueCounts.firstKey()
    val highestKnownValue = valueCounts.lastKey()

    private val smallDelta = let {
        var smallestValueDifference = 1.0
        var prev : Double? = null
        for (value in valueCounts.keys) {
            if (prev != null) {
                val d = value - prev
                if (d < smallestValueDifference) {
                    smallestValueDifference = d
                }
            }
            prev = value
        }
        smallestValueDifference / 2.0
    }

    /**
     * The output value weight is determined to be the estimated "density"
     * at this value.
     */
    override operator fun invoke(origValue: Double): Weighted<Double> {
        val insufficientDataToReweight = valueCounts.size < 2
        return if (insufficientDataToReweight) {
            Weighted(origValue)
        } else {
            val adjustedValue = calculateAdjustedValue(origValue)
            val entryBefore: MutableEntry<Double, Int> = valueCounts.lowerEntry(adjustedValue)
            val keyBefore: Double = entryBefore.key
            val exactCount : Int? = valueCounts.get(adjustedValue)
            val betweenCount: Int = if (exactCount == null) entryBefore.value else (entryBefore.value + exactCount) / 2
            val ceilingKey = valueCounts.ceilingKey(adjustedValue)
            val density = (betweenCount.toDouble()) / (ceilingKey - keyBefore)
            val weight = 1.0 / density
            Weighted(item = origValue, weight = weight)
        }

    }

    private fun calculateAdjustedValue(origValue: Double): Double {
        return if (origValue > lowestKnownValue && origValue < highestKnownValue) {
            origValue
        } else if (origValue <= lowestKnownValue) {
            lowestKnownValue + smallDelta
        } else {
            highestKnownValue - smallDelta
        }
    }

}