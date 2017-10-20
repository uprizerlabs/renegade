package renegade.crossValidation

import mu.KotlinLogging
import java.util.*

/**
 * Created by ian on 7/16/17.
 */

private val logger = KotlinLogging.logger {}

typealias InputOutputPair<T, U> = Pair<T, U>

class CrossValidator<out InputType : Any, out OutputType : Any, in PredictionType : Any>(
        private val splitStrategy: SplitStrategy<InputType, OutputType>,
        private val scoringFunction: (PredictionType, OutputType) -> Double,
        private val data: Iterable<InputOutputPair<InputType, OutputType>>
) {
    fun test(predictiveModelBuilder: (List<InputOutputPair<InputType, OutputType>>) -> ((InputType) -> PredictionType)): Double {
        val loss = DoubleSummaryStatistics()
        splitStrategy.split(data).forEach { (training, testing) ->
            logger.info("Building model with ${training.size} items")
            val model = predictiveModelBuilder.invoke(training)
            logger.info("Testing model with ${testing.size} items")
            for ((input, output) in testing) {
                loss.accept(scoringFunction.invoke(model.invoke(input), output))
            }
        }
        loss.apply {
            logger.info("Tested on $count test datums, loss is $average")
        }
        return loss.average
    }
}

data class TrainTest<InputType : Any, OutputType : Any>(
        val training: ArrayList<InputOutputPair<InputType, OutputType>>,
        val testing: ArrayList<InputOutputPair<InputType, OutputType>>
)

class CorrectClassificationProportion<in OutputType : Any> : (OutputType, OutputType) -> Double {
    override fun invoke(output: OutputType, classification: OutputType) = if (output == classification) 1.0 else 0.0

}

interface SplitStrategy<InputType : Any, OutputType : Any> {
    fun split(data: Iterable<InputOutputPair<InputType, OutputType>>): Sequence<TrainTest<InputType, OutputType>>
}

