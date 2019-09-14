package renegade.opt

import mu.KotlinLogging
import renegade.util.math.random
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

// private val usedLabels = ConcurrentSkipListSet<String>()

abstract class OptimizableParameter<T : Any>(val type : KClass<T>, open val label : String, open val default : T) : Serializable {

    init {
//        require(usedLabels.add(label)) { "Label '$label' already used, OptimizableParameter labels must be unique" }
    }

    abstract fun randomSample() : T

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

data class IntRangeParameter(override val label : String, val range : IntRange, override val default : Int) : OptimizableParameter<Int>(Int::class, label, default) {
    override fun randomSample() = range.random()
}

data class DoubleRangeParameter(override val label : String, val range : Pair<Double, Double>, override val default : Double = min(range.second, max(1.0, range.first))) : OptimizableParameter<Double>(Double::class, label, default) {

    override fun randomSample(): Double = (random.nextDouble() * (range.second - range.first)) + range.first
}

data class ValueListParameter<T : Any>(override val label : String, val values : List<T>) : OptimizableParameter<T>(values.first()::class as KClass<T>, label, values.first()) {
    override fun randomSample(): T = values.random()

    constructor(label : String, vararg k : T) : this(label, k.toList())

}
