package renegade.util

import io.kotest.core.spec.style.FreeSpec

/**
 * Created by ian on 7/8/17.
 */
class ListUtilsSpec : FreeSpec() {
    init {
        "should work" {
            val list = (0 .. 10).toList()
            list.sampleDistinctPairs.take(10).forEach { println(it) }
        }
    }
}