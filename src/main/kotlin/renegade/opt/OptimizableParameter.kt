package renegade.opt

import mu.KotlinLogging
import renegade.util.math.random
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

// private val usedLabels = ConcurrentSkipListSet<String>()

abstract class OptimizableParameter<T : Any>() : Serializable {

    abstract val type : KClass<T>

    abstract val label : String

    abstract val default : T

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

data class IntRangeParameter(override val label : String, val range : IntRange, override val default : Int) : OptimizableParameter<Int>() {
    override val type: KClass<Int>
        get() = Int::class


    override fun randomSample() = range.random()
}

data class DoubleRangeParameter(override val label : String, val range : Pair<Double, Double>, override val default : Double = min(range.second, max(1.0, range.first))) : OptimizableParameter<Double>() {
    override val type: KClass<Double>
        get() = Double::class

    override fun randomSample(): Double = (random.nextDouble() * (range.second - range.first)) + range.first
}

data class ValueListParameter<T : Any>(override val label : String, val values : List<T>) : OptimizableParameter<T>() {
    override val type: KClass<T>
        get() = values.first()::class as KClass<T>

    override val default: T = values.first()

    override fun randomSample(): T = values.random()

    constructor(label : String, vararg k : T) : this(label, k.toList())

}
