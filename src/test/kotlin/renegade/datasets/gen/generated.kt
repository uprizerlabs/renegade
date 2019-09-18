package renegade.datasets.gen

import java.lang.Math.*
import java.util.*

fun sigmoidData(): ArrayList<Pair<List<Double>, Double>> {
    val data = ArrayList<Pair<List<Double>, Double>>()
    for (x in -40..40) {
        val xv = x.toDouble() / 20.0
        data += Pair(listOf(xv), sigmoid(xv))
    }
    return data
}

fun sigmoid(xv: Double) = 1.0 / (1.0 + pow(E, -xv))

fun gravityData(): ArrayList<InputOutput<Double>> {
    val data = ArrayList<InputOutput<Double>>()
    for (x in -10..10) {
        for (y in -10..10) {
            val distFrom22 = Math.sqrt(pow(y - 2.3, 2.0) + pow(x - 4.1, 2.0))
            data += InputOutput(listOf(x.toDouble(), y.toDouble()), distFrom22)
        }
    }
    return data
}

