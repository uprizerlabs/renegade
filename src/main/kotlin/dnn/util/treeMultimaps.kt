package dnn.util

import com.google.common.collect.TreeMultimap
import kotlin.coroutines.experimental.buildSequence

/**
 * Return values in a TreeMultimap starting with the closest to [key] and progressively
 * getting further away.
 */
fun <V> TreeMultimap<Double, V>.closestTo(key : Double) : Sequence<Pair<V, Double>> {
    val headIterator = asMap().descendingMap().tailMap(key, false).iterator()
    val tailIterator = asMap().tailMap(key).iterator()
    return buildSequence {
        var lastHead = if (headIterator.hasNext()) headIterator.next() else null
        var lastTail = if (tailIterator.hasNext()) tailIterator.next() else null
        while (true) {
            if (lastHead != null && lastTail != null) {
                val headDiff = lastHead.key diff key
                val tailDiff = lastTail.key diff key
                if (headDiff < tailDiff) {
                    yieldAll(lastHead.value.map {it to headDiff})
                    lastHead = if (headIterator.hasNext()) headIterator.next() else null
                } else {
                    yieldAll(lastTail.value.map {it to tailDiff})
                    lastTail = if (tailIterator.hasNext()) tailIterator.next() else null
                }
            } else if (lastHead != null) {
                val headDiff = lastHead.key diff key
                yieldAll(lastHead.value.map {it to headDiff})
                lastHead = if (headIterator.hasNext()) headIterator.next() else null
            } else if (lastTail != null) {
                val tailDiff = lastTail.key diff key
                yieldAll(lastTail.value.map {it to tailDiff})
                lastTail = if (tailIterator.hasNext()) tailIterator.next() else null
            } else {
                break
            }
        }
    }

}

infix fun Double.diff(a : Double) = Math.abs(a-this)