package dnn.util.math

import java.util.*

operator fun Number.minus(o : Number) = this.toDouble() - o.toDouble()

val Int.sqr get() = this.toLong()*this

val Double.sqr get() = this*this

val Double.abs get() = Math.abs(this)

val Double.sqrt get() = Math.sqrt(this)

data class AveragingAccumulator(val sum : Double, val count : Int) {
    constructor() : this(0.0, 0)

    val avg = sum/count
    operator fun plus(v : Double) = AveragingAccumulator(sum + v, count + 1)
}

infix fun List<Double>.distanceTo(other : List<Double>) : Double {
    require(this.size == other.size, {"List sizes must match"})
    return this.indices.map {(this[it]-other[it]).sqr}.sum().sqrt
}

val random = Random()