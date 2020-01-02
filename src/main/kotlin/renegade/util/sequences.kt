package renegade.util

import mu.KotlinLogging
import java.util.*
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

data class Prioritized<T, P : Comparable<P>>(val item : T, val priority : P)

fun <T, C : Comparable<C>> Sequence<Prioritized<T, C>>.priorityBuffer(bufferSize: Int): Sequence<Prioritized<T, C>> {
    // TODO: Could the optimal buffer size be determined statistically somehow?
    val src = this.iterator()
    return sequence {
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

fun <K> Sequence<K>.lookAheadHighest(lookAhead: Int = 5, minimum : Int = 0, valueExtractor: (K) -> Double): IndexedValue<K>? {
    return this.lookAheadLowest(lookAhead = lookAhead, minimum = minimum, valueExtractor = { -valueExtractor(it) })
}

fun <K> Sequence<K>.lookAheadLowest(lookAhead: Int = 5, minimum : Int = 0, valueExtractor: (K) -> Double): IndexedValue<K>? {
    var best: IndexedValue<Pair<K, Double>>? = null
    for (valWithIndex in this.withIndex()) {
        if (best != null && valWithIndex.index - best.index > lookAhead) break
        val thisVal = valueExtractor(valWithIndex.value)
        if (valWithIndex.index >= minimum) {
            if (best == null || best.value.second > thisVal) {
                best = IndexedValue(valWithIndex.index, valWithIndex.value to thisVal)
            }
        }
    }
    return if (best != null) IndexedValue(best.index, best.value.first) else null
}

class TrainTest<E>(val train: List<E>, val test: List<E>)

fun <E : Any> List<E>.splitTrainTest(trainProportion: Double = 0.9): TrainTest<E> {

    val shuffled = this.shuffled()

    val separationIndex = (trainProportion * this.size).roundToInt()
    return TrainTest(shuffled.subList(0, separationIndex), shuffled.subList(separationIndex, shuffled.size))
}

fun <K> Sequence<K>.toPairSequence() : Sequence<Pair<K, K>> {
    val previousValues = ArrayList<K>()
    return sequence {
        for (next in this@toPairSequence) {
            for (pair in previousValues) {
                yield (next to pair)
                yield (pair to next)
            }
            previousValues += next
        }
    }
}