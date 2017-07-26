package dnn.indexes.smallWorld

import java.lang.ref.WeakReference
import kotlin.coroutines.experimental.buildSequence

data class Node<ItemType : Any>(val item: ItemType) {

    private val neighborsBySampleRecency = ArrayList<WeakReference<Node<ItemType>>>()

    fun getNeighbors(allNodes : RandomAccessSet<Node<ItemType>>): Set<Node<ItemType>> {
        return synchronized(neighborsBySampleRecency) {
            buildSequence<Node<ItemType>> {
                neighborsBySampleRecency.toList().forEach { nodeRef ->
                    val node = nodeRef.get()
                    if (node == null || !allNodes.contains(node)) {
                        neighborsBySampleRecency.removeIf { it.get() == node }
                    } else {
                        yield(node) // !! shouldn't be necessary but it is, perhaps coroutines bug
                    }
                }
            }.toSet()
        }
    }

    fun sampleDestination(node: Node<ItemType>, outdegree: Int) {
        // TODO: ArrayList might be a little inefficient here, particularly
        // TODO: for large values of maxDegree
        synchronized(neighborsBySampleRecency) {
            neighborsBySampleRecency.removeIf { node == it.get() }
            neighborsBySampleRecency.add(0, WeakReference(node))
            while (neighborsBySampleRecency.size > outdegree) {
                neighborsBySampleRecency.removeAt(neighborsBySampleRecency.size - 1)
            }
        }
    }
}