package dnn.search

import dnn.util.Two

/**
 * Created by ian on 7/4/17.
 */
abstract class MetricSpaceIndex<ItemType : Any>(val distanceFunction : (Two<ItemType>) -> Double) {
    abstract fun searchFor(item : ItemType) : Sequence<Result<ItemType>>

    abstract fun add(item : ItemType)

    interface Result<out ItemType> {
        val item : ItemType
        val distance : Double

        fun remove()
    }
}