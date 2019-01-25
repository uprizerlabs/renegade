package renegade.indexes.bucketing

import renegade.util.Two
import java.io.Serializable
import java.util.*
import java.util.stream.Collectors

class ItemBucketer<ItemType : Any, in DistanceType : Comparable<DistanceType>>(
        private val distanceFunction: (Two<ItemType>) -> DistanceType,
        samples: Collection<ItemType>,
        private val bits : Int = 8
) : Serializable {

    val waypointPairs = samples.shuffled().take(bits * 2).windowed(size = 2, step = 2, partialWindows = false)

    fun bucket(item : ItemType) : BitSet {
        val bitset = BitSet(bits)
        waypointPairs.withIndex().toList().parallelStream().map { (ix, pair) ->
            val firstDist = distanceFunction(Two(pair[0], item))
            val secondDist = distanceFunction(Two(pair[1], item))
            ix to (firstDist > secondDist)
        }.collect(Collectors.toList()).forEach { (ix, b) -> bitset[ix] = b }
        return bitset
    }

}