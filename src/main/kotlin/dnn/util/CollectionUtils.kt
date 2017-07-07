package dnn.util

/**
 * Created by ian on 7/3/17.
 */

class Two<out V>(a : V, b : V) {
    private val set = setOf(a, b)

    val first : V get() = set.first()

    val second : V get() = if (set.size == 1) first else set.last()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Two<*>
        if (set != other.set) return false
        return true
    }

    override fun hashCode(): Int {
        return set.hashCode()
    }

    operator fun  component1() = first
    operator fun  component2() = second
}

infix fun <K1, V1, V2> Map<K1, V1>.outerJoin(other : Map<K1, V2>) : Map<K1, Pair<V1?, V2?>> {
    val allKeys = this.keys.union(other.keys)
    return allKeys.map { key -> key to (this[key] to other[key])}.toMap()
}

fun <K1, V1, V2> Map<K1, V1>.innerJoin(other : Map<K1, V2>, complainIfMissing : Boolean = true) : Map<K1, Pair<V1, V2>> {
    val allKeys = this.keys.union(other.keys)
    return allKeys.mapNotNull { key ->
        val v1 = this[key]
        val v2 = other[key]
        if (v1 != null && v2 != null) key to (v1 to v2) else {
            if (complainIfMissing) {
                null
            } else {
                throw RuntimeException("Key $key is missing from ${if (v1 == null) "this" else "other"} map")
            }
        }
    }.toMap()
}