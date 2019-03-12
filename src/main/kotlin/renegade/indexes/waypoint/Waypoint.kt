package renegade.indexes.waypoint

import renegade.util.closestTo
import java.io.Serializable
import java.util.concurrent.*

class HashComparator<T : Any> : Comparator<T>, Serializable {
    override fun compare(o1: T, o2: T): Int {
        return o1.hashCode().compareTo(o2.hashCode())
    }

}

data class Waypoint<ItemType : Any>(
        val item: ItemType
) : Serializable {

    private val distances = ConcurrentSkipListMap<Double, CopyOnWriteArrayList<ItemType>>(Comparator.naturalOrder())

    private val invertedDistances = ConcurrentHashMap<ItemType, Double>()

    fun add(distance : Double, newItem: ItemType) {
        distances.computeIfAbsent(distance) { CopyOnWriteArrayList() }.add(newItem)
    }

    fun items() = distances.values.flatten()

    fun remove(item: ItemType) {
        val dist = invertedDistances.remove(item)
        if (dist != null) {
            distances.values.forEach { it.remove(item) }
        }
    }

    fun closestTo(d: Double) = this.distances.closestTo(d)
}