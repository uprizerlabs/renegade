package dnn.indexes.waypoint

import dnn.indexes.MetricSpaceIndex
import dnn.util.*
import dnn.util.math.distanceTo
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

class WaypointIndex<ItemType : Any>(
        distance: (Two<ItemType>) -> Double,
        waypoints: List<ItemType>,
        private val lookAhead: Int = 100,
        private val vectorLookAhead: Int = lookAhead * 10
) : MetricSpaceIndex<ItemType, Double>(distance) {

    private val logger = KotlinLogging.logger {}

    constructor(distance: (Two<ItemType>) -> Double, numWaypoints: Int, samples: Iterable<ItemType>, lookAhead: Int = 100, vectorLookAhead: Int = lookAhead * 10)
            : this(
            distance,
            samples.asSequence().toMutableList().shuffle().take(numWaypoints), lookAhead, vectorLookAhead
    )

    init {
        if (waypoints.size > 20) {
            logger.warn("${waypoints.size} is probably too many waypoints (warning threshold is 20)")
        }
    }

    private val waypoints = waypoints.map { Waypoint(it)}
    private val itemVectors = ConcurrentHashMap<ItemType, List<Double>>()

    override fun searchFor(soughtItem: ItemType): Sequence<WaypointIndexResult<ItemType>> {

        data class WaypointWithDistance(val ix: Int, val waypoint: ItemType, val distance: Double)

        val waypointDistances = waypoints
                .withIndex()
                .map { (ix, v) -> WaypointWithDistance(ix, v.item, distanceFunction(Two(v.item, soughtItem))) }
        val closest = waypointDistances.minBy { it.distance }!!
        val ix = closest.ix

        val waypointsToTry = waypoints[ix].closestTo(waypointDistances[ix].distance)

        // FIXME: These are already in ascending order, need to calculate the distance vectors to all waypoints
        // and measure their distance instead.  This needs to be broken up.

        val soughtItemVector: List<Double> = waypointDistances.map { it.distance }

        return waypointsToTry
                .map({ (item, _) -> calculateVectorDistance(soughtItemVector, item) })
                .priorityBuffer(vectorLookAhead)
                .map({ (item, _) -> calculateActualDistance(soughtItem, item) })
                .priorityBuffer(lookAhead)
                .map { (item, priority) -> WaypointIndexResult(item, priority, this) }
    }

    internal fun calculateVectorDistance(soughtItemVector: List<Double>, item: ItemType): Prioritized<ItemType, Double> {
        val itemVector = itemVectors[item] ?:
                throw NullPointerException("Item $item not found in itemVectors map")
        val vectorDistance = soughtItemVector distanceTo itemVector
        return Prioritized(item, vectorDistance)
    }

    internal fun calculateActualDistance(soughtItem: ItemType, candidateItem: ItemType): Prioritized<ItemType, Double> {
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
        if (itemToAdd !in itemVectors) {
            val vector = waypoints.map { distanceFunction(Two(it.item, itemToAdd)) }
            itemVectors[itemToAdd] = vector
            waypoints.withIndex().forEach { (index, value) -> value.add(vector[index], itemToAdd) }
        }
    }

    override fun all(): Iterable<ItemType> = waypoints.first().items()

}
