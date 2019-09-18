package renegade.datasets.gen.spiral

import renegade.util.math.random
import java.util.*

class Spiral(val noise: Double = 0.0) {
    fun generate(samples: Int = 400): ArrayList<Pair<List<Double>, Int>> {
        val data = ArrayList<Pair<List<Double>, Int>>()
        genSpiral(samples / 2, 0.0, 1, data) // Positive
        genSpiral(samples / 2, Math.PI, -1, data) // Negative
        return if (noise != 0.0) {
            ArrayList(data.map {
                it.first.map { it + random.nextGaussian() * noise} to  it.second
            })
        } else {
            data
        }
    }

    private fun genSpiral(samples: Int, deltaT: Double, label: Int, addTo: ArrayList<Pair<List<Double>, Int>>) {
        for (i in 0 .. (samples / 2)) {
            val r = i.toDouble() / samples * 5.0
            val t = 2.0* 1.75 * i / samples * 2 * Math.PI + deltaT
            val x = r * Math.sin(t)
            val y = r * Math.cos(t)
            addTo += listOf(x, y) to label
        }
    }
}