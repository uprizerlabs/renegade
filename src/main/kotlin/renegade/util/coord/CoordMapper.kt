package renegade.util.coord

import com.github.sanity.pav.toArrayList
import mu.KotlinLogging
import renegade.MetricSpace
import renegade.opt.*
import renegade.util.Two
import renegade.util.coord.CoordMapper.Parameters.dimensionality
import renegade.util.coord.CoordMapper.Parameters.searchDepth
import kotlin.math.abs
import kotlin.random.Random

private val logger = KotlinLogging.logger {}



class CoordMapper<I : Any, O : Any>(val cfg : OptConfig, val metricSpace: MetricSpace<I, O>, val waypoints: List<I> = findWaypoints(metricSpace, cfg[dimensionality], cfg[searchDepth])) {

    private object Parameters {
        val dimensionality = ValueListParameter("dimensionality", 2, 3, 4, 5)
        val searchDepth = ValueListParameter("searchDepth", 50, 100, 500)
    }

    fun map(input : I) : List<Double> {
        return waypoints.map { metricSpace.estimateDistance(Two(input, it)) }
    }
}

private val r = Random(0)

private fun <I : Any, O : Any> findWaypoints(metricSpace: MetricSpace<I, O>, dimensionality: Int, searchDepth: Int): List<I> {
    val raTraining = metricSpace.trainingData.toArrayList()

    raTraining.shuffle()

    data class Best(val inputs: Two<I>, val distance: Double)

    val initialPair = (0..(searchDepth * 2)).map {
        val a = raTraining.random().first
        val b = raTraining.random().first
        val inputs = Two(a, b)
        Best(inputs, metricSpace.estimateDistance(inputs))
    }.maxByOrNull { it.distance } ?: throw RuntimeException("No initial pair found, training data empty?")

    val waypoints: MutableSet<I> = mutableSetOf(initialPair.inputs.first, initialPair.inputs.second)

    for (n in 2 until dimensionality) {
        val best = raTraining.drop(r.nextInt(raTraining.size - searchDepth)).take(searchDepth).map { candidate ->
            val totalDistance = waypoints.map { waypoint -> abs(metricSpace.estimateDistance(Two(waypoint, candidate.first))) }.sum()
            candidate.first to totalDistance
        }.maxByOrNull { it.second } ?: throw RuntimeException("Unable to find next waypoint")
        waypoints += best.first
    }

    return waypoints.toList()
}
