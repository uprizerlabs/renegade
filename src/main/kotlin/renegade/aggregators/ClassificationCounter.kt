package renegade.aggregators

import java.util.concurrent.atomic.AtomicInteger

class ClassificationCounter<ItemType : Any>(
        private val counterMap: MutableMap<ItemType, AtomicInteger> = HashMap(),
        private val totalCount: AtomicInteger = AtomicInteger(0)
) {

    val total get() = totalCount.get()

    val classes : Set<ItemType> get() = counterMap.keys

    operator fun plusAssign(item: ItemType) {
        pMapCache = null
        totalCount.incrementAndGet()
        counterMap.computeIfAbsent(item, { AtomicInteger(0) }).incrementAndGet()
    }

    private @Volatile var pMapCache: Map<ItemType, Double>? = null

    fun toCountMap() : Map<ItemType, Int> = counterMap.mapValues { it.value.get() }

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
}