package renegade.util

import java.util.concurrent.ConcurrentSkipListMap
import kotlin.collections.MutableMap.MutableEntry

/**
 * Return values in a TreeMultimap starting with the closest to [key] and progressively
 * getting further away.
 */
fun <ItemType, ValListType : List<ItemType>> ConcurrentSkipListMap<Double, ValListType>.closestTo(key : Double) : Sequence<CloseItem<ItemType>> {
    val headIterator = this.descendingMap().tailMap(key, false).iterator()
    val tailIterator = this.tailMap(key).iterator()
    return sequence {
        var lastHead: MutableEntry<Double, ValListType>? = if (headIterator.hasNext()) headIterator.next() else null
        var lastTail = if (tailIterator.hasNext()) tailIterator.next() else null
        while (true) {
            if (lastHead != null && lastTail != null) {
                val headDiff = lastHead.key diff key
                val tailDiff = lastTail.key diff key
                if (headDiff < tailDiff) {
                    lastHead.value.forEach { yield(CloseItem(it, headDiff)) }
                    lastHead = if (headIterator.hasNext()) headIterator.next() else null
                } else {
                    lastTail.value.forEach { yield(CloseItem(it, tailDiff)) }
                    lastTail = if (tailIterator.hasNext()) tailIterator.next() else null
                }
            } else if (lastHead != null) {
                val headDiff = lastHead.key diff key
                lastHead.value.forEach { yield(CloseItem(it, headDiff)) }
                lastHead = if (headIterator.hasNext()) headIterator.next() else null
            } else if (lastTail != null) {
                val tailDiff = lastTail.key diff key
                lastTail.value.forEach { yield(CloseItem(it, tailDiff)) }
                lastTail = if (tailIterator.hasNext()) tailIterator.next() else null
            } else {
                break
            }
        }
    }

}

data class CloseItem<out ItemType>(val item : ItemType, val distance : Double)

infix fun Double.diff(a : Double) = Math.abs(a-this)