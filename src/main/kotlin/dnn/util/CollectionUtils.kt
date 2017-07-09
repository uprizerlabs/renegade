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

    override fun toString() = "[$first, $second]"

    operator fun  component1() = first
    operator fun  component2() = second
}
