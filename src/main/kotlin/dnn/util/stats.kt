package dnn.util

import org.apache.commons.math3.stat.descriptive.moment.Variance

/**
 * Created by ian on 7/7/17.
 */

data class WeightedValue(val value : Double, val weight : Double = 1.0)

private fun Iterable<WeightedValue>.mSummary() : MutableSummary {
    val summary = MutableSummary()
    this.forEach { summary.add(it.value, it.weight)}
    return summary
}

private val sampleVariance = Variance()
private val populationVariance = Variance(false)


fun Iterable<WeightedValue>.summary() : Summary = mSummary()

fun Map<*, Double>.toWeightedProportions(): List<WeightedValue> {
    val totalCount = this.values.sum()
    return this.values.map {WeightedValue(it / totalCount, it)}
}

fun Iterable<WeightedValue>.sampleStdDev() : Double = this.sampleStdDev(this.summary().mean)

fun Iterable<WeightedValue>.sampleStdDev(precomputedMean : Double) : Double {
    // TODO: Inefficient.  Ideally use an online version - couldn't find one that worked
    // TODO: so decided that slow was better than wrong
    return Math.sqrt(sampleVariance.evaluate(
            this.map {it.value}.toDoubleArray(),
            this.map {it.weight}.toDoubleArray(), precomputedMean
    ))
}

fun Iterable<WeightedValue>.populationStdDev() : Double = this.populationStdDev(this.summary().mean)

fun Iterable<WeightedValue>.populationStdDev(precomputedMean : Double) : Double {
    // TODO: Inefficient.  Ideally use an online version - couldn't find one that worked
    // TODO: so decided that slow was better than wrong
    return Math.sqrt(populationVariance.evaluate(
            this.map {it.value}.toDoubleArray(),
            this.map {it.weight}.toDoubleArray(), precomputedMean
    ))
}

interface Summary {
    val count : Double
    val mean : Double
    val sum : Double
}

private class MutableSummary : Summary {
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
