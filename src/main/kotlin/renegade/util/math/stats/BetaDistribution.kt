package renegade.util.math.stats

import renegade.util.math.sqr

class BetaDistribution(val alpha : Double, val beta : Double) {
    val mean : Double get() = alpha / (alpha + beta)

    val variance : Double get() = (alpha * beta) / ((alpha + beta).sqr * (alpha + beta + 1.0))

    val stddev = Math.sqrt(variance)
}