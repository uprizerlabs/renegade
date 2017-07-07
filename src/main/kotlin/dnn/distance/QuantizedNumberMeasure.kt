package dnn.distance

import com.github.sanity.pav.*
import dnn.util.*

class QuantizedNumberMeasure(samples : Iterable<Number>) {
    // TODO: Use reservoir sampler to limit size of orderedSamples

    private val orderedSamples = samples.sortedBy { it.toDouble() }

    fun distance(v : Two<Number>) = Math.abs(calibrate(v.first) - calibrate(v.second))

    private fun calibrate(i : Number) : Number {
        val searchResult = orderedSamples.betterBinarySearch(i, compareBy { it.toDouble() })
        val position = when(searchResult) {
            is BinarySearchResult.Exact -> searchResult.index.toDouble()
            is BinarySearchResult.Between -> searchResult.lowIndex + 0.5
        }
        return position / orderedSamples.size
    }
}