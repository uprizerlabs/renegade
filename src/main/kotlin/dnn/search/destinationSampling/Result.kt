package dnn.search.destinationSampling

import dnn.search.MetricSpaceIndex.Result

class Result<out ItemType : Any, DistanceType : Comparable<DistanceType>>(
        override val item: ItemType,
        override val distance: DistanceType,
        val toRemove: () -> Unit) : Result<ItemType, DistanceType> {
    override fun remove() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}