package renegade.util

import java.util.*
import kotlin.coroutines.experimental.buildSequence

data class Prioritized<T, P : Comparable<P>>(val item : T, val priority : P)

fun <T, C : Comparable<C>> Sequence<Prioritized<T, C>>.priorityBuffer(bufferSize: Int): Sequence<Prioritized<T, C>> {
    // TODO: Could the optimal buffer size be determined statistically somehow?
    val src = this.iterator()
    return buildSequence {
        val queue = PriorityQueue<Prioritized<T, C>>(compareBy { it.priority })
        while (true) {
            while (queue.size < bufferSize && src.hasNext()) {
                queue += src.next()
            }
            if (queue.isEmpty()) break
            yield(queue.poll())
        }
    }
}

fun <K> Sequence<K>.lookAheadHighest(lookAhead: Int = 5, valueExtractor: (K) -> Double): IndexedValue<K>?
        = this.lookAheadLowest(lookAhead = lookAhead, valueExtractor = { -valueExtractor(it) })

fun <K> Sequence<K>.lookAheadLowest(lookAhead: Int = 5, valueExtractor: (K) -> Double): IndexedValue<K>? {
    var best: IndexedValue<Pair<K, Double>>? = null
    for (valWithIndex in this.withIndex()) {
        if (best != null && valWithIndex.index - best.index > lookAhead) break
        val thisVal = valueExtractor(valWithIndex.value)
        if (best == null || best.value.second > thisVal) {
            best = IndexedValue(valWithIndex.index, valWithIndex.value to thisVal)
        }
    }
    return if (best != null) IndexedValue(best.index, best.value.first) else null
}

fun <K> Sequence<K>.toPairSequence() : Sequence<Pair<K, K>> {
    val previousValues = ArrayList<K>()
    return buildSequence {
        for (next in this@toPairSequence) {
            for (pair in previousValues) {
                yield (next to pair)
                yield (pair to next)
            }
            previousValues += next
        }
    }
}