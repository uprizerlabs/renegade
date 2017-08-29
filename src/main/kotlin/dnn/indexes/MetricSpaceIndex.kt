package dnn.indexes

import dnn.util.Two

/**
 * Created by ian on 7/4/17.
 */
abstract class MetricSpaceIndex<ItemType : Any, DistanceType : Comparable<DistanceType>>(val distanceFunction : (Two<ItemType>) -> DistanceType) {
    abstract fun searchFor(item : ItemType) : Sequence<out Result<ItemType, DistanceType>>

    abstract fun add(item : ItemType)

    open fun addAll(items : Iterable<ItemType>) = items.forEach{ add(it) }

    interface Result<out ItemType, DistanceType : Comparable<DistanceType>> {
        val item : ItemType
        val distance : DistanceType

        fun remove()
    }

    abstract fun all() : Iterable<ItemType>
}
