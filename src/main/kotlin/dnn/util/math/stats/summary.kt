package dnn.util.math.stats

interface Summary {
    val count : Double
    val mean : Double
    val sum : Double
}

internal class MutableSummary : Summary {
    private var sum_ = 0.0
    private var count_ = 0.0

    override val count get() : Double = count_
    override val mean get() : Double = sum_ / count_
    override val sum get() : Double = sum_

    fun add(value : Double, weight : Double = 1.0) {
        sum_ += value*weight
        count_ += weight
    }

    operator fun plusAssign(v : Double) = add(v)
}
