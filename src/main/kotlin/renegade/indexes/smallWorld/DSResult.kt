package renegade.indexes.smallWorld

import renegade.indexes.MetricSpaceIndex.Result

class DSResult<out ItemType : Any, DistanceType : Comparable<DistanceType>>(
        override val item: ItemType,
        override val distance: DistanceType,
        val measurementCount : Int,
        val toRemove: () -> Unit) : Result<ItemType, DistanceType> {
    override fun toString() = "$item dist: $distance meas: $measurementCount"

    override fun remove() {
        toRemove()
    }

}