package dnn.search.destinationSampling

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by ian on 7/6/17.
 */
class RandomAccessSet<V> {
    private val random = Random()
    private val arrayList = Collections.synchronizedList(ArrayList<V>())
    private val positionMap = ConcurrentHashMap<V, Int>()

    operator fun plusAssign(i: V) {
        if (!positionMap.containsKey(i)) {
            synchronized(arrayList) {
                val position = arrayList.size
                arrayList += i
                positionMap[i] = position
            }
        }
    }

    operator fun minusAssign(i: V) {
        synchronized(arrayList) {
            val ix = positionMap.remove(i)
            if (ix != null) {
                arrayList.removeAt(ix)
            }
        }
    }

    fun contains(i : V) = positionMap.containsKey(i)

    fun random(filter: (V) -> Boolean = { true }): V? =
            synchronized(arrayList) {
                for (attempts in 0..5) {
                    val candidate = arrayList[random.nextInt(arrayList.size)]
                    if (filter(candidate)) return candidate
                }
                return arrayList.firstOrNull(filter)
            }
}
