package dnn.search

import dnn.util.Two
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by ian on 7/4/17.
 */
class ExhaustiveMSI<IT : Any>(distanceFunction: (Two<IT>) -> Double) : MetricSpaceIndex<IT>(distanceFunction) {
    private val nextIndex = AtomicInteger(0)

    override fun add(item: IT) {
        val index = nextIndex.getAndIncrement()
        items[index] = item
    }

    private val items = ConcurrentHashMap<Int, IT>()

    override fun searchFor(item : IT): Sequence<Result<IT>> {
        return items.entries.map { distanceFunction(Two(it.value, item)) to it}
                .sortedBy { it.first }
                .asSequence()
                .map { (distance, mapEntry) ->
                    val (index, item) = mapEntry
                    Result(item, distance, this, index)
                }
    }

    class Result<out IT : Any>(
            override val item: IT,
            override val distance: Double,
            private val parent : ExhaustiveMSI<IT>,
            private val index : Int
    ) : MetricSpaceIndex.Result<IT> {
        override fun remove() {
            parent.items[index]
        }
    }
}