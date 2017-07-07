package dnn.search.destinationSampling

import dnn.search.MetricSpaceIndex
import dnn.util.Two
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.experimental.buildSequence

/**
 * An efficient algorithm for searching a metric space.  Uses
 * [destination sampling](https://arxiv.org/pdf/math/0702325.pdf) which is based on an
 * [old idea of mine](https://freenetproject.org/papers/ddisrs.pdf)
 *
 * @author [Ian Clarke](http://blog.locut.us/))
 */

private val uidGenerator = AtomicLong(0)



class DestinationSamplingMetricSpaceIndex<ItemType : Any>(
        distanceFunction: (Two<ItemType>) -> Double,
        private val outdegree: Int = 10,
        private val lookAhead: Int = 100) : MetricSpaceIndex<ItemType>(distanceFunction) {
    // Maintain strong references to non-deleted nodes
    private val nodes = RandomAccessSet<Node<ItemType>>()

    override fun add(item: ItemType) {
        val greeters = searchFor(item, false).take(outdegree)
        val newNode = Node(item)
        nodes += newNode
        greeters.toList().parallelStream().forEach {
            newNode.sampleDestination(it.node, outdegree)
            it.node.sampleDestination(newNode, outdegree)
        }
    }

    private data class ResultWithNode<ItemType : Any>(val result: Result<ItemType>, val node: Node<ItemType>)

    override fun searchFor(item: ItemType): Sequence<Result<ItemType>> {
        return searchFor(item, true).map { it.result }
    }

    private fun searchFor(item: ItemType, shouldSampleDestinations: Boolean): Sequence<ResultWithNode<ItemType>> {
        val sequence : Sequence<ResultWithNode<ItemType>> = buildSequence {
            val nodeMeasurements = HashMap<Node<ItemType>, NodeDistance<ItemType>>()
            val toVisit = sortedSetOf(compareBy<NodeDistance<ItemType>>({ it.distance }, { System.identityHashCode(it.node) }))
            val visited = mutableSetOf<NodeDistance<ItemType>>()
            val toEmit = sortedSetOf(compareBy<NodeDistance<ItemType>>({ it.distance }, { System.identityHashCode(it.node) }))

            fun measure(node: Node<ItemType>) {
                val distance = distanceFunction(Two(item, node.item))
                if (!nodeMeasurements.contains(node)) {
                    // Since this is the only way nodes are added to the toVisit or toEmit queues this condition
                    // ensures that nodes cannot be visited or emitted more than once
                    val nodeDistance = NodeDistance(node, distance)
                    nodeMeasurements[node] = nodeDistance
                    toVisit += nodeDistance
                    toEmit += nodeDistance
                }
            }

            fun NodeDistance<ItemType>.toResult(): ResultWithNode<ItemType> =
                    ResultWithNode(Result(node.item, distance, {nodes -= node}), node)

            fun getRandomUnmeasuredNode(): Node<ItemType>? = nodes.random { !nodeMeasurements.containsKey(it) }

            fun visitNext() {
                val visiting = toVisit.pollFirst()!!
                visiting.node.getNeighbors(nodes).forEach { measure(it) }
                visited += visiting
            }

            var emitted = 0
            sequence@ while (true) {
                while (toEmit.size > lookAhead) {
                    val result = toEmit.pollFirst().toResult()
                    val firstToBeEmitted = emitted == 0
                    if (shouldSampleDestinations && firstToBeEmitted) {
                        visited.forEach { it.node.sampleDestination(result.node, outdegree) }
                    }
                    yield(result)
                    emitted++
                }
                if (toVisit.isEmpty()) {
                    val randomNode = getRandomUnmeasuredNode()
                    if (randomNode == null) {
                        // I don't think there is any point in sampling destinations here or incrementing emitted
                        yieldAll(toEmit.map { it.toResult() })
                        break@sequence
                    } else {
                        measure(randomNode)
                    }
                }
                visitNext()
            }
        }


        return sequence.constrainOnce()
    }
}

data class NodeDistance<ItemType : Any>(val node: Node<ItemType>, val distance: Double)

