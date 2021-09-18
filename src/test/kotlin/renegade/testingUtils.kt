package renegade

import io.kotest.matchers.doubles.plusOrMinus
import java.util.*

/**
 * Created by ian on 7/9/17.
 */

private val tolerance = 0.00001
fun approx(n : Number) = (n.toDouble().plusOrMinus(tolerance))

operator fun DoubleSummaryStatistics.plusAssign(v : Double) = this.accept(v)