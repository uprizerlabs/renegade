package dnn.util

import com.google.common.collect.*

/**
 * Created by ian on 7/5/17.
 */

fun <K, V> Map<K, List<V>>.toMultimap() : Multimap<K, V> = Multimaps.newListMultimap(this, {ArrayList()})

fun <K, V> Iterable<Pair<K, V>>.toMultimap(): ArrayListMultimap<K, V> {
    val mm = ArrayListMultimap.create<K, V>()
    this.forEach { (k, v) -> mm.put(k, v) }
    return mm
}

fun <K, V> MutableMap<K, V>.noOverwrite() = dnn.util.NoOverwriteMutableMap(this)
class NoOverwriteMutableMap<K, V>(private val mmap : MutableMap<K, V>) : MutableMap<K, V> by mmap {
    override fun put(key: K, value: V): V? {
        require(!this.containsKey(key))
        return mmap.put(key, value)
    }
}