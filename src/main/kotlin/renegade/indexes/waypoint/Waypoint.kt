package renegade.indexes.waypoint

import com.google.common.collect.TreeMultimap
import renegade.util.closestTo
import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal data class Waypoint<ItemType : Any>(
        val item: ItemType
) {

    private val distances = TreeMultimap.create<Double, ItemType>(Comparator.naturalOrder(), compareBy { it.hashCode() })

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