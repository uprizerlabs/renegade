package renegade.supervised

import mu.KotlinLogging
import renegade.MetricSpace
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.opt.OptConfig
import renegade.util.Two
import java.io.Serializable

private val logger = KotlinLogging.logger {}

internal fun <InputType : Any, OutputType : Any> buildDistanceFunction(cfg : OptConfig,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>,
        trainingData: List<Pair<InputType, OutputType>>
): (Two<InputType>) -> Double {
    val metricSpace = MetricSpace(
            cfg = cfg,
            modelBuilders = distanceModelBuilders,
            trainingData = trainingData,
            outputDistance = { a, b -> if (a == b) 0.0 else 1.0 }
    )

    metricSpace
            .modelContributions
            .lastEntry()
            ?.value
            ?.map { metricSpace.distanceModelList[it.key] to it.value }
            ?.sortedByDescending { it.second }
            ?.take(10)?.let { top10 ->
                logger.info("Top 10 contributing distance models:")
                logger.info("label\tscore")
                for (it in top10) {
                    logger.info("${it.first.label}\t${it.second}")
                }
    }

    return object : (Two<InputType>) -> Double, Serializable {
        override fun invoke(pairs: Two<InputType>): Double {
            return metricSpace.estimateDistance(Two(pairs.first, pairs.second))
        }

    }
}
