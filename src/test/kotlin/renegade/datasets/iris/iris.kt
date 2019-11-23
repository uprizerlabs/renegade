package renegade.datasets.iris

import mu.KotlinLogging
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.*
import renegade.opt.OptConfig
import renegade.supervised.*
import java.util.zip.GZIPInputStream

/**
 * Created by ian on 7/15/17.
 */

// sepal_length,sepal_width,petal_length,petal_width,species

private val logger = KotlinLogging.logger {}

fun main() {

    val data = loadIrisDataset()

    val classifier = irisClassifier(data)

}

data class IrisMeasurements(val sepalLength: Double, val sepalWidth: Double, val petalLength: Double, val petalWidth: Double)

enum class IrisSpecies {
    setosa, versicolor, virginica
}

fun loadIrisDataset(): List<Pair<IrisMeasurements, IrisSpecies>> {
    return GZIPInputStream(IrisMeasurements::class.java.getResourceAsStream("iris.csv.gz")).bufferedReader().lineSequence().drop(1).map { line ->
        val columns = line.split(',')
        fun dcol(x: Int) = columns[x].toDouble()
        val species = IrisSpecies.valueOf(columns[4])
        IrisMeasurements(dcol(0), dcol(1), dcol(2), dcol(3)) to species
    }.toList()
}

fun irisClassifier(data : List<Pair<IrisMeasurements, IrisSpecies>>): SlowClassifier<IrisMeasurements, IrisSpecies> {
    val builders = ArrayList<DistanceModelBuilder<IrisMeasurements>>()
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::petalLength)
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::petalWidth)
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::sepalLength)
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::sepalWidth)
/*
    for (builder in ArrayList(builders)) {
        for (exp in listOf(0.25, 0.5, 0.75, 1.25, 1.5, 2.0, 4.0)) {
            builders += DoubleDistanceModelBuilder(label = "petalLength^$exp").map { it -> Math.pow(it.petalLength, exp) }
            builders += DoubleDistanceModelBuilder(label = "petalWidth^$exp").map { it  -> Math.pow(it.petalWidth, exp) }
            builders += DoubleDistanceModelBuilder(label = "sepalLength^$exp").map { it  -> Math.pow(it.sepalLength, exp) }
            builders += DoubleDistanceModelBuilder(label = "sepalWidth^$exp").map { it  -> Math.pow(it.sepalWidth, exp) }

        }
    }
*/
/*
    builders += AdvancedDoubleDistanceModelBuilder("adv-petalLength").map(IrisMeasurements::petalLength)
    builders += AdvancedDoubleDistanceModelBuilder("adv-petalWidth").map(IrisMeasurements::petalWidth)
    builders += AdvancedDoubleDistanceModelBuilder("adv-sepalLength").map(IrisMeasurements::sepalLength)
    builders += AdvancedDoubleDistanceModelBuilder("adv-sepalWidth").map(IrisMeasurements::sepalWidth)
*/
    val classifier = buildSlowClassifier(OptConfig(), data, builders)

    return classifier
}

private infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
    require(start.isFinite())
    require(endInclusive.isFinite())
    require(step > 0.0) { "Step must be positive, was: $step." }
    val sequence = generateSequence(start) { previous ->
        if (previous == Double.POSITIVE_INFINITY) return@generateSequence null
        val next = previous + step
        if (next > endInclusive) null else next
    }
    return sequence.asIterable()
}