package dnn.indexes.smallWorld

import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 7/6/17.
 */
class RandomAccessSetSpec : FreeSpec() {
    init {
        "Given a RandomAccessSet containing 3 elements" {
            val set = RandomAccessSet<Int>()
            set += 1
            set += 2
            set += 3
            set.contains(1) shouldBe true
            set.contains(2) shouldBe true
            set.contains(3) shouldBe true
            set.contains(4) shouldBe false
            set -= 2
            set.contains(1) shouldBe true
            set.contains(2) shouldBe false
            set.contains(3) shouldBe true
            set.contains(4) shouldBe false

            val retrieved = HashSet<Int>()
            for (x in 0 .. 100) {
                retrieved.add(set.random()!!)
                if (retrieved.size == 2) break
            }
            retrieved.shouldEqual(setOf(1, 3))
        }
    }
}