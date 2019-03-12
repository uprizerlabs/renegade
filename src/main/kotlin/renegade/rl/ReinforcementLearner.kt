package renegade.rl

import java.util.concurrent.*

sealed class ActionValue {
    data class Integer(val minimum : Int?, val maximum : Int?) : ActionValue()
    data class Category<T : Any>(val values : List<T>)
}

class ReinforcementLearner(val historyStore: HistoryStore = MemoryHistoryStore(), val actionSchema : Map<String, ActionValue>) {

}

interface HistoryStore {
    fun record(instance: Instance)

    fun retrieve(): List<Instance>
}

class MemoryHistoryStore : HistoryStore {
    private val instances = ConcurrentLinkedQueue<Instance>()

    override fun record(instance: Instance) {
        instances += instance
    }

    override fun retrieve(): List<Instance> = instances.asSequence().toList()

}

data class Instance(val context: TypedMap, val action: TypedMap, val outcome: TypedMap)

class TypedMap {
    private val map = ConcurrentHashMap<TypedKey<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: TypedKey<T>): T? = map[key] as T?

    operator fun <T : Any> set(key: TypedKey<T>, value: T) {
        map[key] = value
    }

}

data class TypedKey<T : Any>(val name: String, val value: T)