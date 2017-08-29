package dnn.indexes.lsh

import dnn.indexes.MetricSpaceIndex
import dnn.util.Two

class LshIndex<ItemType : Any, DistanceType : Comparable<DistanceType>>(distanceFunction: (Two<ItemType>) -> DistanceType, val pivots: List<ItemType>) : MetricSpaceIndex<ItemType, DistanceType>(distanceFunction) {


    override fun all(): Iterable<ItemType> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun add(item: ItemType) {
        TODO()
    }

    override fun searchFor(item: ItemType): Sequence<Result<ItemType, DistanceType>> {
        TODO()
    }
}