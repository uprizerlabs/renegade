package renegade.indexes.smallWorld

import renegade.indexes.MetricSpaceIndex
import renegade.util.Two
import kotlin.coroutines.experimental.buildSequence

/**
 * An efficient heuristic algorithm for searching a
 * [generalized hypermetric space](https://en.wikipedia.org/wiki/Metric_space#Generalizations_of_metric_spaces) for
 * items that are closest to a sought item.  Closeness is defined by a distance function supplied by
 * the user of this class.
 *
 * The items are represented as a directed graph, with edges determined through
 * [destination sampling](https://arxiv.org/pdf/math/0702325.pdf) (based on
 * [Freenet-style routing](https://freenetproject.org/papers/ddisrs.pdf)).  Destination
 * sampling should ensure that the graph maintains the [small world](https://en.wikipedia.org/wiki/Small-world_network)
 * property, which should facilitate fast renegade.indexes.
 *
 * Search is performed through greedy best-first with backtracking.  It is not guaranteed to find the closest element,
 * but it should in most cases.  The renegade.indexes returns a lazy [Sequence] of item results, which will get steadily
 * further away from the sought item, each subsequent item should be the closest to the item sought that hasn't
 * already been returned (but this is not guaranteed).
 *
 * @author [Ian Clarke](http://blog.locut.us/))
 */

class SmallWorldMetricSpaceIndex<ItemType : Any, DistanceType : Comparable<DistanceType>>(
        distanceFunction: (Two<ItemType>) -> DistanceType,
        private val outdegree: Int = 10,
        private val lookAhead: Int = 100) : MetricSpaceIndex<ItemType, DistanceType>(distanceFunction) {
    override fun all(): Iterable<ItemType> = nodes.all().map {it.item}

    // Maintain strong references to non-deleted nodes
    private val nodes = RandomAccessSet<Node<ItemType>>()

    override fun add(item: ItemType) {
        val greeters = searchFor(item, false).take(outdegree).toList()
        val newNode = Node(item)
        nodes += newNode
        greeters.forEach {
            newNode.sampleDestination(it.node, outdegree)
            it.node.sampleDestination(newNode, outdegree)
        }
    }

    private data class ResultWithNode<ItemType : Any, DistanceType : Comparable<DistanceType>>(val result: DSResult<ItemType, DistanceType>, val node: Node<ItemType>)

    override fun searchFor(item: ItemType): Sequence<DSResult<ItemType, DistanceType>> {
        return searchFor(item, true).map { it.result }
    }

    private fun searchFor(item: ItemType, shouldSampleDestinations: Boolean): Sequence<ResultWithNode<ItemType, DistanceType>> {
        val sequence : Sequence<ResultWithNode<ItemType, DistanceType>> = buildSequence {
            val nodeMeasurements = HashMap<Node<ItemType>, NodeDistance<ItemType, DistanceType>>()
            val toVisit = sortedSetOf(compareBy<NodeDistance<ItemType, DistanceType>>({ it.distance }, { System.identityHashCode(it.node) }))
            val visited = mutableSetOf<NodeDistance<ItemType, DistanceType>>()
            val toEmit = sortedSetOf(compareBy<NodeDistance<ItemType, DistanceType>>({ it.distance }, { System.identityHashCode(it.node) }))

            fun measure(node: Node<ItemType>) {
                if (!nodeMeasurements.contains(node)) {
                    // Since this is the only way nodes are added to the toVisit or toEmit queues this condition
                    // ensures that nodes cannot be visited or emitted more than once
                    val distance = distanceFunction(Two(item, node.item))
                    val nodeDistance = NodeDistance(node, distance, nodeMeasurements.size - 1)
                    nodeMeasurements[node] = nodeDistance
                    toVisit += nodeDistance
                    toEmit += nodeDistance
                }
            }

            fun NodeDistance<ItemType, DistanceType>.toResult(): ResultWithNode<ItemType, DistanceType> =
                    ResultWithNode(DSResult(node.item, distance, this.measurementCount, {nodes -= node}), node)

            fun getRandomUnmeasuredNode(): Node<ItemType>? = nodes.random { !nodeMeasurements.containsKey(it) }

            fun visitNext() {
                val visiting = toVisit.pollFirst()!!
                visiting.node.getNeighbors(nodes).toList().forEach { measure(it) }
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

data class NodeDistance<ItemType : Any, DistanceType : Comparable<DistanceType>>(
        val node: Node<ItemType>,
        val distance: DistanceType,
        val measurementCount: Int) {
    override fun toString() = "$node-$distance"
}

