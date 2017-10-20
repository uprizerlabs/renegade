package renegade.features

import renegade.features.InputType.DoubleInputType

interface FeatureXformerFactory {
    val inputTypes: List<InputType>

    val isomorphicToParentFeature: Boolean

    val name: String

    fun createFeatureXformer(survey: List<List<Any>>): FeatureXformer
}

sealed class InputType {
    abstract fun match(values: Sequence<Any>): Boolean

    class DoubleInputType : InputType() {
        override fun match(values: Sequence<Any>)
                = values.all { it is Number }
    }
}

interface FeatureXformer {
    fun transform(inputs: List<Any>): Double
}

class MathUniOp(
        override val name: String,
        override val isomorphicToParentFeature: Boolean,
        val op: (Double) -> Double)
    : FeatureXformerFactory {

    override val inputTypes = listOf<InputType>(DoubleInputType())

    override fun createFeatureXformer(survey: List<List<Any>>): FeatureXformer {
        return object : FeatureXformer {
            override fun transform(inputs: List<Any>): Double {
                require(inputs.size == 1)
                val a = inputs[0] as Double
                return op(a)
            }
        }
    }
}
/*
class BiOpWithConstant(val biOps: List<MathBiOp>, val constants: List<Any>) : FeatureXformerFactory {
    override val inputTypes = listOf(DoubleInputType)
}

class MathBiOp(
        override val name: String,
        val op: (Double, Double) -> Double)
    : FeatureXformerFactory {

    override val inputTypes = listOf<KClass<out Any>>(Double::class, Double::class)

    override val isomorphicToParentFeature = false

    override fun createFeatureXformer(survey: List<List<Any>>): FeatureXformer {
        return object : FeatureXformer {
            override fun transform(inputs: List<Any>): Double {
                require(inputs.size == 2)
                val a = inputs[0] as Double
                val b = inputs[1] as Double
                return op(a, b)
            }
        }
    }
}

val featureXFormerFactories = listOf<FeatureXformerFactory>(
        MathBiOp("add", { a, b -> a + b }),
        MathBiOp("subtract", { a, b -> a + b }),
        MathBiOp("multiply", { a, b -> a + b }),
        MathBiOp("divide", { a, b -> a + b })

)
        */