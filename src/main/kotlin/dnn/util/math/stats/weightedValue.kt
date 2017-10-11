package dnn.util.math.stats

import org.apache.commons.math3.stat.descriptive.moment.Variance

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