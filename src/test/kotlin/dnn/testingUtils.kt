package dnn

import io.kotlintest.matchers.plusOrMinus

/**
 * Created by ian on 7/9/17.
 */

private val tolerance = 0.00001
fun approx(n : Number) = (n.toDouble().plusOrMinus(tolerance))