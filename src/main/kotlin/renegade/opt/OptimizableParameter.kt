package renegade.opt

import mu.KotlinLogging
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

private val usedLabels = ConcurrentSkipListSet<String>()

abstract class OptimizableParameter<T : Any>(val type : KClass<T>, open val label : String) {

    init {
        require(usedLabels.add(label)) { "Label '$label' already used, OptimizableParameter labels must be unique" }
    }

    abstract fun minimise(history : Map<T, Double> = emptyMap()) : T?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OptimizableParameter<*>

        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }

}

data class IntRangeParameter(override val label : String, val range : IntRange) : OptimizableParameter<Int>(Int::class, label) {
    override fun minimise(history: Map<Int, Double>): Int? = range.firstOrNull { !history.containsKey(it) }
}

data class ValueListParameter<T : Any>(override val label : String, val values : List<T>) : OptimizableParameter<T>(values.first()::class as KClass<T>, label) {

    constructor(label : String, vararg k : T) : this(label, k.toList())

    override fun minimise(history: Map<T, Double>): T? = values.firstOrNull { !history.containsKey(it) }
}
