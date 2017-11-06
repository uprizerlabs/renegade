package renegade.indexes

import renegade.util.Two
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * A simple but very inefficient [MetricSpaceIndex]
 */
class ExhaustiveMetricSpaceIndex<ItemType : Any, DistanceType : Comparable<DistanceType>>(distanceFunction: (Two<ItemType>) -> DistanceType)
    : MetricSpaceIndex<ItemType, DistanceType>(distanceFunction) {
    override fun all() = items.values

    private val nextIndex = AtomicInteger(0)

    override fun add(item: ItemType) {
        val index = nextIndex.getAndIncrement()
        items[index] = item
    }

    private val items = ConcurrentHashMap<Int, ItemType>()

    override fun searchFor(item : ItemType): Sequence<renegade.indexes.MetricSpaceIndex.Result<ItemType, DistanceType>> {
        return items.entries.map { distanceFunction(Two(it.value, item)) to it}
                .sortedBy { it.first }
                .asSequence()
                .map { (distance, mapEntry) ->
                    val (index, item) = mapEntry
                    Result(item, distance, this, index)
                }
    }

    class Result<out ItemType : Any, DistanceType : Comparable<DistanceType>>(
            override val item: ItemType,
            override val distance: DistanceType,
            private val parent : ExhaustiveMetricSpaceIndex<ItemType, DistanceType>,
            private val index : Int
    ) : MetricSpaceIndex.Result<ItemType, DistanceType> {
        override fun toString() = "ix: $index, item: $item, distance: $distance"

        override fun remove() {
            parent.items[index]
        }
    }
}