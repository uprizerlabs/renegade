package dnn.indexes.waypoint

import dnn.approx
import dnn.util.Two
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

class WaypointSpec : FreeSpec() {
    init {
        "Given a waypoint" - {
            val waypoint = Waypoint(5)
            ".item is as expected" {
                waypoint.item shouldBe 5
            }
            "add() and closest() work as expected" {
                val distFunc = {(a, b) : Two<Int> -> Math.abs(a-b).toDouble()}
                waypoint.add(distFunc(Two(5, 8)), 8)
                waypoint.add(distFunc(Two(5, 9)), 9)
                val results = waypoint.closestTo(3.0).toList()
                results.size shouldBe 2
                results[0].first shouldBe 8
                results[0].second shouldBe approx(3.0)
                results[1].first shouldBe 9
                results[1].second shouldBe approx(4.0)
            }
        }
    }
}