package renegade.crossValidation

import mu.KotlinLogging
import renegade.util.math.random
import java.util.*

private val logger = KotlinLogging.logger {}

class SimpleSplitStrategy<InputType : Any, OutputType : Any>(val testProp: Double) : SplitStrategy<InputType, OutputType> {
    override fun split(data: Iterable<InputOutputPair<InputType, OutputType>>): Sequence<TrainTest<InputType, OutputType>> {
        return sequence {
            val training = ArrayList<InputOutputPair<InputType, OutputType>>()
            val testing = ArrayList<InputOutputPair<InputType, OutputType>>()
            data.forEach { datum ->
                if (random.nextDouble() < testProp) {
                    testing += datum
                } else {
                    training += datum
                }
            }
            logger.info("Testing ${training.size} items against ${testing.size} items")
            yield(TrainTest(training, testing))

        }
    }
}

    class FoldSplitStrategy<InputType : Any, OutputType : Any>(private val folds: Int, private val limit: Int? = null) : SplitStrategy<InputType, OutputType> {
        private val largePrimeNumber = 10007

        override fun split(data: Iterable<InputOutputPair<InputType, OutputType>>): Sequence<TrainTest<InputType, OutputType>> {
            val foldsWithLimit = if (limit != null) Math.min(folds, limit) else folds
            return sequence {
                for (fold in 0..foldsWithLimit) {
                    logger.info("Splitting on fold $fold of $foldsWithLimit")
                    val training = ArrayList<InputOutputPair<InputType, OutputType>>()
                    val testing = ArrayList<InputOutputPair<InputType, OutputType>>()
                    for (datum in data) {
                        val isTesting = hashToFold(datum) == fold
                        if (isTesting) {
                            testing += datum
                        } else {
                            training += datum
                        }
                    }
                    yield(TrainTest(training, testing))
                }
            }
        }

        private fun hashToFold(datum: InputOutputPair<InputType, OutputType>) = Math.abs(datum.hashCode()).rem(largePrimeNumber).rem(folds)
    }