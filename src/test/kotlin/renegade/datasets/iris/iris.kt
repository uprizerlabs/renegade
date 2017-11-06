package renegade.datasets.iris

import mu.KotlinLogging
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.supervised.Classifier
import java.util.zip.GZIPInputStream

/**
 * Created by ian on 7/15/17.
 */

// sepal_length,sepal_width,petal_length,petal_width,species

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    val classifier = irisClassifier()
/*
    loadIrisDataset().forEach { pair ->
        classifier.

    }

    val crossValidator = CrossValidator<IrisMeasurements, IrisSpecies>(SimpleSplitStrategy(0.1), CorrectClassificationProportion, data)
*/
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

fun irisClassifier(): Classifier<IrisMeasurements, IrisSpecies> {
    val data = loadIrisDataset()
    val builders = ArrayList<DistanceModelBuilder<IrisMeasurements>>()
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::petalLength)
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::petalWidth)
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::sepalLength)
    builders += DoubleDistanceModelBuilder().map(IrisMeasurements::sepalWidth)

    val classifier = Classifier(data, builders)

    return classifier
}