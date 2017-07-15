package dnn.util

import java.util.*

operator fun Number.minus(o : Number) = this.toDouble() - o.toDouble()

val Int.sqr : Long get() = this.toLong()*this

val Double.sqr : Double get() = this*this

data class AveragingAccumulator(val sum : Double, val count : Int) {
    constructor() : this(0.0, 0)

    val avg = sum/count
    operator fun plus(v : Double) = AveragingAccumulator(sum + v, count + 1)
}

val random = Random()