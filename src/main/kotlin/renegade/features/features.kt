package renegade.features

import kotlin.math.*

abstract class FeatureExtractor : (List<Double?>) -> Double? {
}

class RawFeature(val ix: Int) : FeatureExtractor() {
    override fun invoke(p1: List<Double?>): Double? {
        return p1[ix]
    }

    override fun toString() = "F$ix"
}

class BiopExtractor(val biop: (Double, Double) -> Double, val name: String, val infix: Boolean, val leftFT: FeatureExtractor, val rightFT: FeatureExtractor) : FeatureExtractor() {
    override fun invoke(p1: List<Double?>): Double? {
        val a = leftFT(p1)
        val b = rightFT(p1)
        return if (a != null && b != null) {
            val r = biop(a, b)
            if (r.isFinite()) r else 0.0
        } else {
            null
        }
    }

    override fun toString() = if (infix) {
        "($leftFT $name $rightFT)"
    } else {
        "$name($leftFT, $rightFT)"
    }
}

fun generateBiops(left: FeatureExtractor, right: FeatureExtractor): List<BiopExtractor> {
    val list = ArrayList<BiopExtractor>()
    list += BiopExtractor(Double::plus, "+", true, left, right)
    list += BiopExtractor(Double::times, "*", true, left, right)
    list += BiopExtractor(Double::div, "/", true, left, right)
    list += BiopExtractor(::max, "max", false, left, right)
    list += BiopExtractor(::min, "min", false, left, right)
    list += BiopExtractor({ a, b -> max(a, b) - min(a, b) }, "xor", false, left, right)

    return list
}
