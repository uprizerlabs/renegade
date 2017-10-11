package dnn.indexes.waypoint

import dnn.approx
import dnn.indexes.*
import dnn.util.*
import dnn.util.math.*
import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec
import mu.KotlinLogging

class WaypointIndexSpec : FreeSpec() {

    private val logger = KotlinLogging.logger {}

    init {
        "given a small dataset of 2 points" - {
            val points = ArrayList<Point>()
            points += Point(1.0, 2.0)
            points += Point(3.0, 1.0)
            "create a WayPointIndex with 2 different waypoints" - {
                val waypoints = listOf(Point(3.0, 4.0), Point(2.0, 3.0))
                val waypointIndex = WaypointIndex({ (a, b) : Two<Point> -> a.dist(b)}, waypoints)
                waypointIndex.addAll(points)
                val point = Point(1.0, 2.0)
                "test calculateVectorDistance()" {
                    val relVector = listOf(4.0, 5.0)
                    val result = waypointIndex.calculateVectorDistance(relVector, point)
                    result.item shouldBe point
                    val vectorDistance = result.priority
                    val w0dist = waypoints[0].dist(point)
                    val w1dist = waypoints[1].dist(point)
                    val pointVector = listOf(w0dist, w1dist)
                    vectorDistance shouldBe approx(pointVector.distanceTo(relVector))
                }
                "test calculateActualDistance()" {
                    val lookingFor = Point(3.2, 1.2)
                    val result = waypointIndex.calculateActualDistance(lookingFor, point)
                    result.item shouldBe point
                    result.priority shouldBe approx(point.dist(lookingFor))
                }
            }
        }

        "given a 2D point dataset with 10000 points" - {
            val points = ArrayList<Point>()
            for (x in 0 .. 10000) {
                points += Point(random.nextDouble(), random.nextDouble())
            }
            var compareCount = 0
            "given a WayPointIndex with 4 waypoints, and an exhaustive index" - {
                val distanceFunction: (Two<Point>) -> Double = {
                    compareCount++
                    val distance = it.first.dist(it.second)
                    distance
                }
                val waypointIndex = WaypointIndex(distance = distanceFunction, numWaypoints = 4, samples = points)
                val exhaustiveIndex = ExhaustiveMetricSpaceIndex(distanceFunction)
                points.forEach { exhaustiveIndex.add(it); waypointIndex.add(it) }
                "search for 100 random points and measure accuracy and distance calls of each" {
                    data class DistanceAccuracy(val distance : Double, val time : Int)
                    fun searchWith(index : MetricSpaceIndex<Point, Double>) : DistanceAccuracy {
                        compareCount = 0
                        val target = Point(random.nextDouble(), random.nextDouble())
                        val result = index.searchFor(target).first()
                        val distance = result.item.dist(target)
                        val time = compareCount
                        return DistanceAccuracy(distance, time)
                    }
                    val exhaustiveAvgDist = (0 .. 100).map {
                        val exhaustiveResults = searchWith(exhaustiveIndex)
                        exhaustiveResults.distance
                    }.average()
                    val waypointAvgDist = (0 .. 100).map {
                        val waypointResults = searchWith(waypointIndex)
                        waypointResults.distance
                    }.average()
                    logger.info("Exhaustive average distance: $exhaustiveAvgDist, waypoint average distance: $waypointAvgDist")
                    waypointAvgDist should beLessThanOrEqualTo(exhaustiveAvgDist * 2)
                }
            }
        }

    }
    data class Point(val x : Double, val y : Double) {
        infix fun dist(o : Point) : Double = ((this.x - o.x).sqr + (this.y - o.y).sqr).sqrt
    }
}