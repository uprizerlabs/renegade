package renegade.indexes.bucketing

import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec

class ItemBucketerSpec : FreeSpec() {
    init {
        "given an ItemBucketer" - {
            val ib = ItemBucketer({ (a, b) -> Math.abs(a - b) }, listOf(1.0, 2.0), 1)
            "verify similar inputs are bucketed similarily" {
                val a = ib.bucket(1.1)
                val b = ib.bucket(1.2)
                 a[0] shouldEqual b[0]
            }
            "verify dissimilar inputs are bucketed dissimilarily" {
                ib.bucket(1.1)[0] shouldNotBe ib.bucket(1.9)[0]
            }
        }
    }
}