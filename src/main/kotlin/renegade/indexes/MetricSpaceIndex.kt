package renegade.indexes

import renegade.util.Two
import java.io.Serializable

/**
 * Store and search for items stored in a defined metric space
 */
abstract class MetricSpaceIndex<ItemType : Any, DistanceType : Comparable<DistanceType>>(val distanceFunction : (Two<ItemType>) -> DistanceType) : Serializable {
    abstract fun searchFor(item : ItemType) : Sequence<out Result<ItemType, DistanceType>>

    abstract fun add(item : ItemType)

    open fun addAll(items: Collection<ItemType>) = items.parallelStream().forEach {
        requireNotNull(it); add(it)
    }

    interface Result<out ItemType, DistanceType : Comparable<DistanceType>> {
        val item : ItemType
        val distance : DistanceType

        fun remove()
    }

    abstract fun all() : Iterable<ItemType>
}
