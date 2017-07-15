package dnn.indexes

import dnn.indexes.smallWorld.*
import dnn.util.*
import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec
import mu.KotlinLogging

/**
 * Created by ian on 7/5/17.
 */

private val logger = KotlinLogging.logger {}

class SmallWorldMetricSpaceIndexSpec : FreeSpec() {

    init {

        data class Point(val x: Double, val y: Double) {
            override fun toString() = "($x, $y)"
        }

        fun distance(a: Point, b: Point) = Math.sqrt((a.x - b.x).sqr + (a.y - b.y).sqr)

        "A SmallWorldMetricSpaceIndex initialized with a 2D Euclidean dist metric" - {
            "on creating MSI" - {
                val freenetMSI = SmallWorldMetricSpaceIndex<Point, Double>({ (a, b) -> distance(a, b) })
                freenetMSI.add(Point(1.0, 2.0))
                "should be the only one retrieved in a dnn.indexes" {
                    val results = freenetMSI.searchFor(Point(1.0, 2.0)).toList()
                    results.size shouldBe 1
                    val result = results.first()
                    result.item shouldBe Point(1.0, 2.0)
                    result.distance shouldBe (distance(Point(1.0, 2.0), Point(1.0, 2.0)) plusOrMinus 0.0000001)
                }
            }
        }

        "Given a DSMSI intialized with a 2D coordinate distance metric" - {
            fun distance(points: Two<Point>): Double
                    = Math.sqrt((points.first.x - points.second.x).sqr + (points.first.y - points.second.y).sqr)

            val dsmsi = SmallWorldMetricSpaceIndex(::distance, lookAhead = 100)
            "on inserting 100000 random points" - {
                val points = (0..100000).map { Point(random.nextDouble(), random.nextDouble()) }.toMutableList()
                points.forEach { dsmsi.add(it) }
                "on retrieving `elephant`" {
                    val searchPoint = Point(0.5, 0.5)
                    points.sortBy { distance(Two(searchPoint, it)) }
                    logger.info("Actual closest 5:")
                    points.asSequence().take(5).map { "$it\t${distance(Two(it, searchPoint))}" }.toList().forEach {
                        logger.info(it)
                    }
                    val results: List<DSResult<Point, Double>> = dsmsi.searchFor(searchPoint).take(5).toList()
                    logger.info("Found closest 5:")
                    results.forEach {
                        logger.info("${it.item}\t${distance(Two(it.item, searchPoint))}\t${it.measurementCount}")
                    }
                }
            }
        }

    }

}