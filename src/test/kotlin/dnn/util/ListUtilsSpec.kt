package dnn.util

import io.kotlintest.specs.FreeSpec

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