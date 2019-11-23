package renegade.datasets.bodyfat

import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import java.util.zip.GZIPInputStream

data class Bodyfat(val headers : List<String>, val data : List<Pair<List<Double>, Double>>) {
    fun createBuilders(): ArrayList<DistanceModelBuilder<List<Double>>> {
        val builders = ArrayList<DistanceModelBuilder<List<Double>>>()
        headers.withIndex().forEach { (ix, header) ->
            builders += DoubleDistanceModelBuilder(label = header).map { it[ix] }
        }
        return builders
    }
}

fun loadBodyfatDataset(): Bodyfat {
    return GZIPInputStream(Bodyfat::class.java.getResourceAsStream("560_bodyfat.tsv.gz")).bufferedReader().readLines().let { lines ->
        val dataWithHeader = lines.map { it.split('\t') }
        val headersWithTarget = dataWithHeader[0]
        if (headersWithTarget.last() != "target") error("'target' is not last column")
        val headers = headersWithTarget.subList(0, headersWithTarget.size-1)
        val data = dataWithHeader
                .asSequence()
                .drop(1)
                .map { it.subList(0, it.size - 1).map(String::toDouble) to it.last().toDouble() }
                .toList()
        return Bodyfat(headers, data)
    }
}