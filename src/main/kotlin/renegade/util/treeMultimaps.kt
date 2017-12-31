package renegade.util

import com.google.common.collect.TreeMultimap
import kotlin.coroutines.experimental.buildSequence

/**
 * Return values in a TreeMultimap starting with the closest to [key] and progressively
 * getting further away.
 */
fun <ItemType> TreeMultimap<Double, ItemType>.closestTo(key : Double) : Sequence<CloseItem<ItemType>> {
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
                    yieldAll(lastHead.value.map { CloseItem(it, headDiff)})
                    lastHead = if (headIterator.hasNext()) headIterator.next() else null
                } else {
                    yieldAll(lastTail.value.map { CloseItem(it, tailDiff)})
                    lastTail = if (tailIterator.hasNext()) tailIterator.next() else null
                }
            } else if (lastHead != null) {
                val headDiff = lastHead.key diff key
                yieldAll(lastHead.value.map { CloseItem(it, headDiff)})
                lastHead = if (headIterator.hasNext()) headIterator.next() else null
            } else if (lastTail != null) {
                val tailDiff = lastTail.key diff key
                yieldAll(lastTail.value.map { CloseItem(it, tailDiff)})
                lastTail = if (tailIterator.hasNext()) tailIterator.next() else null
            } else {
                break
            }
        }
    }

}

data class CloseItem<out ItemType>(val item : ItemType, val distance : Double)

infix fun Double.diff(a : Double) = Math.abs(a-this)