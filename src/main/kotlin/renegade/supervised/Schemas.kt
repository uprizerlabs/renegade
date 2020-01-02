package renegade.supervised

import org.apache.commons.math3.stat.regression.SimpleRegression
import kotlin.math.abs

object Schemas {

    class RegressionSchema<InputType : Any>(private val extrapolate: Boolean) : DataSchema<InputType, Double, Double>(
            outputDistance = { a, b -> abs(a - b) },
            predictionAggregator = { predictions ->
                if (extrapolate) {
                val reg = SimpleRegression()
                    for ((item, distance) in predictions) {
                        reg.addData(distance, item)
                    }
                reg.intercept
            } else predictions.map { it.item }.average() },
            predictionError = { a, b -> abs(a - b) }
    )

    class ClassifierSchema<InputType : Any, OutputType : Any> : DataSchema<InputType, OutputType, Map<OutputType, Double>>(
            outputDistance = { a, b -> if (a == b) 0.0 else 1.0 },
            predictionAggregator = { p ->
                val size = p.size
                val counts = p.groupingBy { it.item }.eachCount()
                counts.mapValues { it.value.toDouble() / size }
            }
            , predictionError = { o, p -> 1.0 - p.getOrDefault(o, 0.0) }
    )

}
