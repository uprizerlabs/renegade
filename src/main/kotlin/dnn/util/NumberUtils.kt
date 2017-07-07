package dnn.util

operator fun Number.minus(o : Number) = this.toDouble() - o.toDouble()

val Int.sqr : Long get() = this.toLong()*this

val Double.sqr : Double get() = this*this

val Iterable<Pair<Double, Double>>.weightedVariance : Double get() {
    // Could be more efficient: http://jonisalonen.com/2013/deriving-welfords-method-for-computing-variance/
    val totalWeight = this.map {it.second}.sum()
    val totalValue = this.map {it.first * it.second}.sum()
    val mean = totalValue / totalWeight
    val variance = this.map { (it.second - mean).sqr }.average()
    return variance
}

val Iterable<Pair<Double, Double>>.weightedStdDev : Double get() = Math.sqrt(this.weightedVariance)
