package renegade.indexes

import com.eatthepath.jvptree.VPTree
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import renegade.util.Two

class JVPTreeIndex<ItemType : Any>(uncachedDistFunc: (Two<ItemType>) -> Double) : MetricSpaceIndex<ItemType, Double>(uncachedDistFunc) {

    private val distFunc = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .build(object : CacheLoader<Two<ItemType>, Double>() {
                override fun load(key: Two<ItemType>): Double = uncachedDistFunc(key)
            })

    private val vpTree = VPTree<ItemType, ItemType> { a, b -> this.distFunc[Two(a, b)]}

    class JVPResult<out ItemType : Any>(
            override val item: ItemType,
            override val distance: Double
            ) : Result<ItemType, Double> {
        override fun remove() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun searchFor(item: ItemType): Sequence<Result<ItemType, Double>> {
        return vpTree.getNearestNeighbors(item, 100)
                .asSequence()
                .map { JVPResult(it, distFunc[Two(it, item)]) }
    }

    override fun add(item: ItemType) {
        vpTree += item
    }

    override fun all(): Iterable<ItemType> = vpTree
}