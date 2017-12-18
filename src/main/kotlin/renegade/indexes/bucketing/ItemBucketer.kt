package renegade.indexes.bucketing

import renegade.util.Two
import java.io.Serializable
import java.util.*

class ItemBucketer<ItemType : Serializable, in DistanceType : Comparable<DistanceType>>(
        private val distanceFunction: (Two<ItemType>) -> DistanceType,
        samples: Collection<ItemType>,
        private val bits : Int = 8
) {

    val waypointPairs = samples.shuffled().take(bits * 2).windowed(size = 2, step = 2, partialWindows = false)

    fun bucket(item : ItemType) : BitSet {
        val bitset = BitSet(bits)
        waypointPairs.withIndex().forEach { (ix, pair) ->
            val firstDist = distanceFunction(Two(pair[0], item))
            val secondDist = distanceFunction(Two(pair[1], item))
            bitset[ix] = firstDist > secondDist
        }
        return bitset
    }

}