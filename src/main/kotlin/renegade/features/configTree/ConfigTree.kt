package renegade.features.configTree

data class ConfigTree<N : Any>(val node : Node<N>)

interface Node<N : Any> {
    val attributes : Attributes

    val value : N

    val children : ChildNodes<N>
}

sealed class ChildNodes<N : Any> {
    data class Ordered<N : Any>(val sequence : Sequence<Node<N>>) : ChildNodes<N>()

    data class Unordered<N : Any>(val nodes : Set<Node<N>>) : ChildNodes<N>()
}

class Attributes (val doubles : Map<String, Double> = HashMap(), val categories : Map<String, Any> = HashMap()


)

class ConfigTreeExplorer<N : Any>

