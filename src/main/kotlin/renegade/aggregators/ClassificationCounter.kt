package renegade.aggregators

import com.google.common.util.concurrent.AtomicDouble
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

class ClassificationCounter<ItemType : Any>(
        private val counterMap: MutableMap<ItemType, AtomicDouble> = ConcurrentHashMap(),
        private val totalCount: AtomicDouble = AtomicDouble(0.0)
) : Serializable {

    val total get() = totalCount.get()

    val classes : Set<ItemType> get() = counterMap.keys

    operator fun plusAssign(item: ItemType) {
        addWithCount(item, 1.0)
    }

    fun addWithCount(item: ItemType, count : Double = 1.0) {
        pMapCache = null
        totalCount.addAndGet(count)
        counterMap.computeIfAbsent(item) { AtomicDouble(0.0) }.addAndGet(count)
    }

    private @Volatile var pMapCache: Map<ItemType, Double>? = null

    fun toCountMap() : Map<ItemType, Double> = counterMap.mapValues { it.value.get() }

    fun toProbabilityMap(): Map<ItemType, Double> {
        return pMapCache.let {
            if (it == null) {
                val probMap = counterMap.mapValues { it.value.get().toDouble() / totalCount.get() }
                pMapCache = probMap
                probMap
            } else {
                it
            }
        }
    }

    override fun toString(): String {
        return "ClassificationCounter(${toCountMap()}, ttl: $total"
    }
}