package dnn.util

import dnn.util.math.sqr
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.coroutines.experimental.buildSequence

/**
 * Created by ian on 7/3/17.
 */

val <A> List<A>.sampleDistinctPairs: Sequence<Two<A>> get() {
    val list = this
    require(this is RandomAccess)
    require(this.size < Math.sqrt(Long.MAX_VALUE.toDouble()))
    val listSize = list.size
    return buildSequence {
        while (true) {
            // In a perfect world would ensure uniqueness of random numbers using something like
            // https://www.tikalk.com/xincrol-unique-and-random-number-generation-algorithm/
            // But that would be the definition of premature optimization
            val r = ThreadLocalRandom.current().nextLong(listSize.sqr)
            val first = (r / listSize).toInt()
            val second = (r.rem(listSize)).toInt()
            if (first != second) {
                yield(Two(list[first], list[second]))
            }
        }
    }
}

fun <A> List<A>.replace( at: Int, with : A) : List<A> {
    val mList = ArrayList(this)
    mList[at] = with
    return mList
}

fun <T> MutableList<T>.shuffle() : MutableList<T> {
    Collections.shuffle(this)
    return this
}