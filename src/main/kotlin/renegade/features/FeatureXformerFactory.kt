package renegade.features

import renegade.features.InputType.DoubleInputType

/**

Notes to self:
Types of feature search strategy:
 * Constant feature
   * First thought: meta-strategy for selecting between different types of random number generator
     HOWEVER since constants can be combined with each-other to create constants from different
     distributions this shouldn't be necessary - just choose numbers that are as distant as possible
     from numbers already selected - perhaps works if numbers are considered in a circular 1D space,
     then we just pick the number at random but limited to within the largest gap
   * Or, combine "familiarity" search with novelty search using a thompson sampling meta selection strategy
 * Input feature
 * Function application on 1 or two existing features, can have global meta-selection strategy or
 *   per function meta-selection strategy, and choose between them using another meta-selection strategy.

 Use a meta-meta-strategy similar to thompson sampling to select from the *sample* distribution of each.
 * How can we determine
 * Use Renegade itself as a [nearest neighbor smoother](https://en.wikipedia.org/wiki/Kernel_smoother#Nearest_neighbor_smoother)
 * Create a "novelty seeker" as alternate to Regressor and Classifier


 * Pentius specific:
   * Sequence prediction is a generalization of the feature search strategies that can be applied to solve
     Pentius's problem.
   * Create distance extractors and features parameterized (like closest match to X to time - Y)

 Business model
 * LGPL the code to execute the Renegade model (where new data can be added), but keep the code to build
 * it closed source - OR...
 * Keep the code to build it GPL'd, and then allocate bounties to developers in proportion to any improvement
 * they're able to achieve on Renegade datasets.
 * Will need to partition code required to execute the model, and LGPL it.
 * People can pay to upload datasets and build a "manifold" for that dataset.  Amount paid will determine
 *  prioritization of your dataset as we make internal decisions about developmental direction.

 * Profit margin should be structured so as to ensure that "buy" is always the more rational decision versus
 * "build", and building can be achieved simply through feature pre-processing.
 *
 * Does this allow GPL'ing of main codebase?
 *
 * Could this be a pareto optimal business model?
 */

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