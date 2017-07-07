package dnn.search

import dnn.util.Two
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by ian on 7/4/17.
 */
class ExhaustiveMSI<ItemType : Any, DistanceType : Comparable<DistanceType>>(distanceFunction: (Two<ItemType>) -> DistanceType)
    : MetricSpaceIndex<ItemType, DistanceType>(distanceFunction) {
    private val nextIndex = AtomicInteger(0)

    override fun add(item: ItemType) {
        val index = nextIndex.getAndIncrement()
        items[index] = item
    }

    private val items = ConcurrentHashMap<Int, ItemType>()

    override fun searchFor(item : ItemType): Sequence<dnn.search.MetricSpaceIndex.Result<ItemType, DistanceType>> {
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
            private val parent : ExhaustiveMSI<ItemType, DistanceType>,
            private val index : Int
    ) : MetricSpaceIndex.Result<ItemType, DistanceType> {
        override fun remove() {
            parent.items[index]
        }
    }
}