package renegade.util

import java.io.Serializable

/**
 * Represents a set of two values (which may be identical)
 */

class Two<out V>(a : V, b : V) : Serializable {
    private val set = setOf(a, b)

    val first = set.first()

    val second = set.last()

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
