package renegade.supervised

import com.eatthepath.jvptree.VPTree
import mu.KotlinLogging
import renegade.MetricSpace
import renegade.aggregators.ItemWithDistance
import renegade.opt.IntRangeParameter
import renegade.opt.OptConfig
import renegade.opt.ValueListParameter
import renegade.supervised.WaypointLearner.Parameters.insetSize
import renegade.supervised.WaypointLearner.Parameters.numWaypoints
import renegade.supervised.WaypointLearner.Parameters.waypointSearchLimit
import renegade.util.Two
import renegade.util.math.sqr
import renegade.util.math.sqrt
import kotlin.math.pow

private val logger = KotlinLogging.logger {}

class WaypointLearner<InputType : Any, OutputType : Any, PredictionType : Any>(
        override val cfg: OptConfig,
        override val schema: DataSchema<InputType, OutputType, PredictionType>
) : Learner<InputType, OutputType, PredictionType>(cfg, schema) {

    object Parameters {
        val numWaypoints = IntRangeParameter("numWaypoints", 3 .. 10, 3)
        val insetSize = IntRangeParameter("insetSize", 3 .. 50, 10)
        val waypointSearchLimit = IntRangeParameter("waypointSearchLimit", 10 .. 200, 100)
    }

    override fun learn(metric: MetricSpace<InputType, OutputType>, data: List<Pair<InputType, OutputType>>): LearnedModel<InputType, OutputType, PredictionType> {
        val waypoints = waypointSearch(metric, data.map { it.first }, cfg[numWaypoints])

        val vpt = VPTree<Pair<List<Double>, OutputType?>, Pair<List<Double>, OutputType?>> { a, b -> a.first.withIndex().map { (it.value - b.first[it.index]).sqr }.average() }

        val mappedData = data
                .map { datum ->
                    waypoints.map { wp ->
                        metric.estimateDistance(Two(wp, datum.first)) } to datum.second
                }
        vpt.addAll(mappedData)

        return WaypointModel(cfg, vpt, waypoints, schema, metric)
    }

    private fun waypointSearch(metric: MetricSpace<InputType, OutputType>, inputData: List<InputType>, numWaypoints: Int): List<InputType> {
        val initialPairs = sequence {
            val a = inputData.random()
            val b = inputData.random()
            if (a != b) {
                yield(Two(a, b) to metric.estimateDistance(Two(a, b)))
            }
        }

        val bestPair = initialPairs.take(100).maxBy { it.second }

        if (bestPair == null) {
            return emptyList()
        } else {

            logger.info("Initial waypoint distance: ${bestPair!!.second}")

            val waypoints = ArrayList<InputType>()

            waypoints += bestPair!!.first.first
            waypoints += bestPair!!.first.second
            while (waypoints.size < numWaypoints) {

                data class Best(val i: InputType, val dist: Double)

                var best: Best? = null
                for (t in 0..cfg[waypointSearchLimit]) {
                    val candidate = inputData.random()
                    if (candidate !in waypoints) {
                        val totalDistance = waypoints.map { metric.estimateDistance(Two(it, candidate)).sqr }.sum().sqrt
                        if ((best == null) || (best.dist > totalDistance)) {
                            best = Best(candidate, totalDistance)
                        }
                    }
                }
                if (best != null) {
                    logger.info("Waypoint ${waypoints.size}: $best")
                    waypoints += best.i
                }
            }

            logger.info("Waypoints: $waypoints")

            return waypoints
        }
    }
}

class WaypointModel<InputType : Any, OutputType : Any, PredictionType : Any>(
        private val cfg: OptConfig,
        private val vpTree: VPTree<Pair<List<Double>, OutputType?>, Pair<List<Double>, OutputType?>>,
        private val waypoints: List<InputType>,
        private val schema: DataSchema<InputType, OutputType, PredictionType>,
        private val metric: MetricSpace<InputType, OutputType>
) : LearnedModel<InputType, OutputType, PredictionType> {
    override fun predict(input: InputType): PredictionType {
        val vector = waypoints.map { metric.estimateDistance(Two(input, it)) }
        val neighbors = vpTree.getNearestNeighbors(vector to null, cfg[insetSize])
        return schema.predictionAggregator(neighbors.mapNotNull { ItemWithDistance(it.second!!, it.first dist vector) })
    }

}

private infix fun List<Double>.dist(o : List<Double>) : Double {
    require(this.size == o.size)
    return o.withIndex().map { (it.value - this[it.index]).sqr }.average().sqrt
}