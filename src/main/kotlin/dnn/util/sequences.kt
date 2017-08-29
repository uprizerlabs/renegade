package dnn.util

import java.util.*
import kotlin.coroutines.experimental.buildSequence

data class Prioritized<T, P : Comparable<P>>(val item : T, val priority : P)

fun <T, C : Comparable<C>> Sequence<Prioritized<T, C>>.priorityBuffer(bufferSize: Int): Sequence<Prioritized<T, C>> {
    val src = this.iterator()
    return buildSequence {
        val queue = PriorityQueue<Prioritized<T, C>>(compareBy { it.priority })
        while (true) {
            while (queue.size < bufferSize && src.hasNext()) {
                queue += src.next()
            }
            if (queue.isEmpty()) break
            yield(queue.poll())
        }
    }
}