package renegade.indexes.smallWorld

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 7/6/17.
 */
class NodeSpec : FreeSpec() {
    init {
        "given a Node" - {
            val allNodes = RandomAccessSet<Node<Double>>()
            val node = Node(0.5)
            allNodes += node
            "sampleDestination works as expected" {
                val neighbor1 = Node(0.2)
                allNodes += neighbor1
                node.sampleDestination(neighbor1, 2)
                node.getNeighbors(allNodes) shouldBe setOf(neighbor1)

                val neighbor2 = Node(0.7)
                allNodes += neighbor2
                node.sampleDestination(neighbor2, 2)
                node.getNeighbors(allNodes) shouldBe setOf(neighbor1, neighbor2)

                val neighbor3 = Node(0.2)
                allNodes += neighbor3
                node.sampleDestination(neighbor3, 2)
                node.getNeighbors(allNodes) shouldBe setOf(neighbor2, neighbor3)

                val neighbor4 = Node(0.8)
                allNodes += neighbor4
                node.sampleDestination(neighbor2, 2)
                node.sampleDestination(neighbor4, 2)
                node.getNeighbors(allNodes) shouldBe setOf(neighbor2, neighbor4)
            }
        }
    }
}