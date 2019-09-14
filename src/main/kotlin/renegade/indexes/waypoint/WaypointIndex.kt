package renegade.indexes.waypoint

import mu.KotlinLogging
import renegade.indexes.MetricSpaceIndex
import renegade.indexes.waypoint.WaypointIndex.Parameters.lookAhead
import renegade.indexes.waypoint.WaypointIndex.Parameters.vecLookAheadMultiplier
import renegade.opt.OptConfig
import renegade.opt.ValueListParameter
import renegade.util.*
import renegade.util.math.distanceTo
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

private val logger = KotlinLogging.logger {}


data class WaypointIndexConfig(
        val lookAhead: Int = 100,
        val vectorLookAhead: Int = lookAhead * 10) : Serializable

class WaypointIndex<ItemType : Any>(
        val cfg : OptConfig,
        val distance: (Two<ItemType>) -> Double,
        val waypoints: List<Waypoint<ItemType>>
) : MetricSpaceIndex<ItemType, Double>(distance), Serializable {

    private object Parameters {
        val numWaypoints = ValueListParameter("numWaypoints", 10, 2, 4, 16, 32)
        val lookAhead = ValueListParameter("lookAhead", 50, 10, 20, 100, 200, 500)
        val vecLookAheadMultiplier = ValueListParameter("vecLookAheadMult", 10, 2, 4, 8, 16)
    }

    constructor(
            cfg : OptConfig,
            distance: (Two<ItemType>) -> Double,
            samples: Iterable<ItemType>,
             config : WaypointIndexConfig = WaypointIndexConfig()
    )
            : this(
            cfg = cfg,
            distance = distance,
            waypoints = samples
                    .asSequence()
                    .toMutableList()
                    .shuffle()
                    .take(cfg[Parameters.numWaypoints])
                    .map { Waypoint(it) }
    )

    init {
        if (waypoints.size > 20) {
            logger.warn("${waypoints.size} is probably too many waypoints (warning threshold is 20)")
        }
    }

    private val itemVectors = ConcurrentHashMap<ItemType, List<Double>>()

    override fun searchFor(soughtItem: ItemType): Sequence<WaypointIndexResult<ItemType>> {

        if (waypoints.first().items().isEmpty()) {
            logger.warn("Searching an empty WaypointIndex, did you forget to add items to it?")
        }

        data class WaypointWithDistance(val ix: Int, val waypoint: ItemType, val distance: Double)

        val waypointDistances = waypoints
                .withIndex().toList()
                .parallelStream()
                .map { (ix, v) -> WaypointWithDistance(ix, v.item, distanceFunction(Two(v.item, soughtItem))) }
                .collect(Collectors.toList())
        val closest = waypointDistances.minBy { it.distance }!!
        val ix = closest.ix

        val waypointsToTry: Sequence<CloseItem<ItemType>> = waypoints[ix].closestTo(waypointDistances[ix].distance)

        // FIXME: These are already in ascending order, need to calculate the distance vectors to all waypoints
        // and measure their distance instead.  This needs to be broken up.

        val soughtItemVector: List<Double> = waypointDistances.map { it.distance }

        return waypointsToTry
                .map { (item, _) -> calculateVectorDistance(soughtItemVector, item) }
                .priorityBuffer(cfg[lookAhead] * cfg[vecLookAheadMultiplier])
                // TODO: Might be advantagous to parallelize this, but would be non-trivial
                .map { (item, _) -> calculateActualDistance(soughtItem, item) }
                .priorityBuffer(cfg[lookAhead])
                .map { (item, priority) -> WaypointIndexResult(item, priority, this) }
    }

    fun calculateVectorDistance(soughtItemVector: List<Double>, item: ItemType): Prioritized<ItemType, Double> {
        val itemVector = itemVectors[item] ?:
                throw NullPointerException("Item $item not found in itemVectors map")
        val vectorDistance = soughtItemVector distanceTo itemVector
        return Prioritized(item, vectorDistance)
    }

    fun calculateActualDistance(soughtItem: ItemType, candidateItem: ItemType): Prioritized<ItemType, Double> {
        val dist = distanceFunction(Two(soughtItem, candidateItem))
        return Prioritized(candidateItem, dist)
    }

    class WaypointIndexResult<out ItemType : Any>(
            override val item: ItemType,
            override val distance: Double,
            private val parent: WaypointIndex<ItemType>
    ) : Result<ItemType, Double> {

        override fun remove() {
            parent.waypoints.forEach { it.remove(item) }
        }
    }

    override fun add(itemToAdd: ItemType) {
        requireNotNull(itemToAdd)
        if (!itemVectors.containsKey(itemToAdd)) {
            val vector = waypoints.map { distanceFunction(Two(it.item, itemToAdd)) }
            itemVectors[itemToAdd] = vector
            waypoints.withIndex().forEach { (index, value) -> value.add(vector[index], itemToAdd) }
        }
    }

    override fun all(): Iterable<ItemType> = waypoints.first().items()

}
