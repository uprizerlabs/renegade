package renegade.supervised

import java.lang.Math.abs
import java.time.*
import java.util.*

fun TreeMap<Instant, Double>.distance(other : TreeMap<Instant, Double>, cutoffTime : Instant) : Double {
    val cutoffDurThis = Duration.between(this.firstKey(), cutoffTime)
    val cutoffDurOther = Duration.between(other.firstKey(), cutoffTime)

    val minCutoff = minOf(cutoffDurThis, cutoffDurOther)

    val thisTrimmed = this.headMap(this.firstKey() + minCutoff)
    val otherTrimmed = other.headMap(other.firstKey() + minCutoff)

    val thisTtlAmount = thisTrimmed.values.sum() / minCutoff.seconds
    val otherTtlAmount = otherTrimmed.values.sum() / minCutoff.seconds

    return abs(thisTtlAmount - otherTtlAmount)
}