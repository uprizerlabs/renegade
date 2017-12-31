package renegade.indexes.waypoint

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec
import renegade.approx

class WaypointSpec : FreeSpec() {
    init {
        "Given a waypoint" - {
            val waypoint = Waypoint(5)
            ".item is as expected" {
                waypoint.item shouldBe 5
            }
            "add() and closest() work as expected" {
                waypoint.add(3.0, 8)
                waypoint.add(4.0, 9)
                val results = waypoint.closestTo(3.0).toList()
                results.size shouldBe 2
                results[0].item shouldBe 8
                results[0].distance shouldBe approx(0.0)
                results[1].item shouldBe 9
                results[1].distance shouldBe approx(1.0)
            }
        }
    }
}