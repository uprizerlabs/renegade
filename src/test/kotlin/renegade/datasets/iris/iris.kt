package renegade.datasets.iris

import mu.KotlinLogging
import renegade.MetricSpace
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.indexes.ExhaustiveMetricSpaceIndex
import renegade.util.Two
import java.util.zip.GZIPInputStream
import kotlin.streams.toList

/**
 * Created by ian on 7/15/17.
 */

// sepal_length,sepal_width,petal_length,petal_width,species

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val msb = irisMetricSpaceBuilder()
    println()
}

data class IrisMeasurements(val sepalLength: Double, val sepalWidth: Double, val petalLength: Double, val petalWidth: Double)

enum class IrisSpecies {
    setosa, versicolor, virginica
}

fun loadIrisDataset(): List<Pair<IrisMeasurements, IrisSpecies>> {
    return GZIPInputStream(IrisMeasurements::class.java.getResourceAsStream("iris.csv.gz")).bufferedReader().lines().skip(1).map { line ->
        val columns = line.split(',')
        fun dcol(x: Int) = columns[x].toDouble()
        val species = IrisSpecies.valueOf(columns[4])
        IrisMeasurements(dcol(0), dcol(1), dcol(2), dcol(3)) to species
    }.toList()
}

fun irisMetricSpaceBuilder() {
    val data = loadIrisDataset()
    val builders = ArrayList<DistanceModelBuilder<IrisMeasurements>>()
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::petalLength)
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::petalWidth)
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::sepalLength)
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::sepalWidth)
    val msb = MetricSpace(builders, data, 100000, 1.0, null, { a, b -> if (a == b) 0.0 else 1.0 })

    val msi = ExhaustiveMetricSpaceIndex<Pair<IrisMeasurements, IrisSpecies>, Double>({msb.estimateDistance(Two(it.first.first, it.second.first))})

    data.forEach { msi.add(it) }

    val testIris = data[19]

    logger.info("Searching for $testIris")

    val results = msi.searchFor(testIris)

    results.forEach { (logger.info("$it")) }
}