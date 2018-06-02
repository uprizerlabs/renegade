package renegade.indexes.waypoint

import com.google.common.collect.TreeMultimap
import renegade.util.closestTo
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

class HashComparator<T : Any> : Comparator<T>, Serializable {
    override fun compare(o1: T, o2: T): Int {
        return o1.hashCode().compareTo(o2.hashCode())
    }

}

data class Waypoint<ItemType : Any>(
        val item: ItemType
) : Serializable {

    private val distances = TreeMultimap.create<Double, ItemType>(Comparator.naturalOrder(), HashComparator())

    private val invertedDistances = distances.entries().map { (dist, item) -> item to dist }.toMap(ConcurrentHashMap())

    fun add(distance : Double, newItem: ItemType) {
        distances.put(distance, newItem)
    }

    fun items() = distances.values()

    fun remove(item: ItemType) {
        val dist = invertedDistances.remove(item)
        if (dist != null) {
            distances.removeAll(dist).remove(item)
        }
    }

    fun closestTo(d: Double) = this.distances.closestTo(d)
}