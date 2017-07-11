package dnn.search

import dnn.search.destinationSampling.DestinationSamplingMetricSpaceIndex
import dnn.util.sqr
import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 7/5/17.
 */
class DestinationSamplingMetricSpaceIndexSpec : FreeSpec() {
    init {

        "A DestinationSamplingMetricSpaceIndex initialized with a 2D Euclidean dist metric" - {
            data class Coord(val x: Double, val y: Double)
            fun distance(a: Coord, b: Coord) = Math.sqrt((a.x - b.x).sqr + (a.y - b.y).sqr)
            "on creating MSI" - {
                val freenetMSI = DestinationSamplingMetricSpaceIndex<Coord, Double>({ (a, b) -> distance(a, b) })
                freenetMSI.add(Coord(1.0, 2.0))
                "should be the only one retrieved in a search" {
                    val results = freenetMSI.searchFor(Coord(1.0, 2.0)).toList()
                    results.size shouldBe 1
                    val result = results.first()
                    result.item shouldBe Coord(1.0, 2.0)
                    result.distance shouldBe (distance(Coord(1.0, 2.0), Coord(1.0, 2.0)) plusOrMinus 0.0000001)
                }
            }
        }
    }

}