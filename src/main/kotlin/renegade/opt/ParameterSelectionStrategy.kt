package renegade.opt

import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import renegade.util.math.random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

typealias Loss = Double

private const val GREEDY_WINDOW_SIZE = 10

interface ParameterSelectionStrategy<P : Any, OP : OptimizableParameter<P>> {
    fun select(param: OP, previous: Map<P, List<Loss>>): P
}

fun selectNext(t : OptimizableParameter<*>, history : Map<Any, List<Loss>>) : Any = when (t) {
    is IntRangeParameter -> IntRangeParameterSelectionStrategy().select(t, history.mapKeys { it.key as Int })
    is DoubleRangeParameter -> DoubleRangeParameterSelectionStrategy().select(t, history.mapKeys { it.key as Double })
    is ValueListParameter -> ValueListParameterSelectionStrategy().select(t as ValueListParameter<Any>, history)
    else -> error("Unrecognized Parameter type: ${t::class}")
}

class IntRangeParameterSelectionStrategy : ParameterSelectionStrategy<Int, IntRangeParameter> {

    override fun select(param: IntRangeParameter, history: Map<Int, List<Loss>>): Int {
        return if (history.keys.size < GREEDY_WINDOW_SIZE) {
            param.randomSample()
        } else {
            val stats =SummaryStatistics().let { ss ->
                history
                        .entries
                        .flatMap { e -> e.value.map { e.key to it } }
                        .sortedBy { it.second }
                        .take(GREEDY_WINDOW_SIZE)
                        .forEach {
                            val f = it.first
                            ss.addValue(f.toDouble())
                        }
                ss
            }
            val sample = ((random.nextGaussian() * stats.standardDeviation) + stats.mean).roundToInt()
            return max(min(sample, param.range.last), param.range.first)
        }
    }

}

class DoubleRangeParameterSelectionStrategy : ParameterSelectionStrategy<Double, DoubleRangeParameter> {

    override fun select(param: DoubleRangeParameter, history: Map<Double, List<Loss>>): Double {
        return if (history.keys.size < GREEDY_WINDOW_SIZE) {
            param.randomSample()
        } else {
            // Only consider the best 5
            val stats = SummaryStatistics().let { ss ->
                history
                        .entries
                        .flatMap { e -> e.value.map { e.key to it } }
                        .sortedBy { it.second }
                        .take(GREEDY_WINDOW_SIZE)
                        .forEach {
                            val f = it.first
                            ss.addValue(f)
                        }
                ss
            }

            val sample = (random.nextGaussian()*stats.standardDeviation)+stats.mean

            return max(min(sample, param.range.second), param.range.first)
        }
    }

}

class ValueListParameterSelectionStrategy : ParameterSelectionStrategy<Any, ValueListParameter<Any>> {
    override fun select(param: ValueListParameter<Any>, previous: Map<Any, List<Loss>>): Any {

        val rarestValue: Pair<Any, Int> = param
                .values
                .map { p -> p to previous.getOrElse(p, { emptyList() }).size }
                .minBy { it.second } ?: error("Couldn't find rarestValue")

        val minScoresPerValue = 2
        return if (rarestValue.second < minScoresPerValue) {
            rarestValue.first
        } else {
            previous.entries
                    .flatMap { e -> e.value.map { e.key to it } }
                    .sortedBy { it.second }
                    .take(GREEDY_WINDOW_SIZE)
                    .toList()
                    .random()
                    .first
        }
    }

}

private fun Iterable<Double>.sampleNormal() : Double {
    val ss = SummaryStatistics()
    this.forEach { ss.addValue(it) }
    return (random.nextGaussian() * ss.standardDeviation) + ss.mean
}