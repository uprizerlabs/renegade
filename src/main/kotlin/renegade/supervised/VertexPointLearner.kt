package renegade.supervised

import com.eatthepath.jvptree.VPTree
import com.google.common.base.Stopwatch
import mu.KotlinLogging
import renegade.MetricSpace
import renegade.aggregators.ItemWithDistance
import renegade.opt.IntRangeParameter
import renegade.opt.OptConfig
import renegade.opt.ValueListParameter
import renegade.supervised.VertexPointLearner.Parameters.sampleSize
import renegade.util.Two
import renegade.util.math.sqr
import renegade.util.math.sqrt
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.math.min
import kotlin.math.pow


private val logger = KotlinLogging.logger {}

class VertexPointLearner<InputType : Any, OutputType : Any, PredictionType : Any>(
        override val cfg: OptConfig,
        override val schema: DataSchema<InputType, OutputType, PredictionType>,
        val insetSizeOverride: Int? = null
) : Learner<InputType, OutputType, PredictionType>(cfg, schema) {

    override fun learn(metric: MetricSpace<InputType, OutputType>, data: List<Pair<InputType, OutputType>>): LearnedModel<InputType, OutputType, PredictionType> {
        val vpIndex: VPTree<Pair<InputType, OutputType?>, Pair<InputType, OutputType?>> = run {
            logger.info("Building vpIndex with ${data.size} training instances")
            val vpt = VPTree<Pair<InputType, OutputType?>, Pair<InputType, OutputType?>> { a, b -> metric.estimateDistance(Two(a.first, b.first)) }
            vpt.addAll(data)
            logger.info("vpIndex built.")
            vpt
        }

        val insetSize = this.insetSizeOverride ?: run {
            val sampleSize = min(cfg[sampleSize], data.size)
            logger.info("Measuring optimal insetSize with sampleSize $sampleSize")
            val samples = data.shuffled().subList(0, sampleSize)

            logger.info("Dumping insetSize experimental data")
            println("size\tRMSE\ttime(s)")
            data class Best(val insetSize: Int, val rmse: Double)

            var bestSoFar: Best? = null
            for (testInsetSize in 2..(sampleSize / 2)) {
                val sw = Stopwatch.createStarted()
                val rmse = samples.map { (sampleInput, sampleOutput) ->
                    val nearest = vpIndex.getNearestNeighbors((sampleInput to null), testInsetSize)
                    val prediction = schema.predictionAggregator(nearest.mapNotNull {
                        ItemWithDistance(it.second
                                ?: error("Output cannot be null"), metric.estimateDistance(Two(it.first, sampleInput)))
                    })
                    schema.predictionError(sampleOutput, prediction).sqr
                }.average().sqrt

                if (bestSoFar == null || rmse < bestSoFar.rmse) {
                    bestSoFar = Best(testInsetSize, rmse)
                }
                println("$testInsetSize\t$rmse\t${sw.elapsed(TimeUnit.MILLISECONDS).toDouble() / 1000.0}")
                if (testInsetSize > bestSoFar.insetSize + 10) {
                    break
                }
            }
            logger.info("Best inset: $bestSoFar")
            bestSoFar!!.insetSize
        }

        return VertexPointModel(cfg, vpIndex, schema, metric, insetSize)
    }


    object Parameters {
        val distanceCacheSize = IntRangeParameter("distanceCacheSize", 1_000..1_000_000, 1_000_000)
        val sampleSize = IntRangeParameter("sampleSize", 100_000..1_000_000, 60_000)
        val multithreadDistance = ValueListParameter("multithreadDistance", true, false)
        val insetSize = IntRangeParameter("insetSize", 5..1000, 20)
    }


}

class VertexPointModel<InputType : Any, OutputType : Any, PredictionType : Any>(
        private val cfg: OptConfig,
        private val vpTree: VPTree<Pair<InputType, OutputType?>, Pair<InputType, OutputType?>>,
        private val schema: DataSchema<InputType, OutputType, PredictionType>,
        private val metric: MetricSpace<InputType, OutputType>,
        private val insetSize: Int
) : LearnedModel<InputType, OutputType, PredictionType> {
    override fun predict(input: InputType): PredictionType {
        val predictions = vpTree.getNearestNeighbors((input to null), insetSize)
                .filterNot { it.first == input }.let {
                    if (cfg[VertexPointLearner.Parameters.multithreadDistance]) {
                        it.parallelStream()
                    } else {
                        it.stream()
                    }.map { (i, o) ->
                        if (o != null) {
                            val distance =
                                    ItemWithDistance(o, metric.estimateDistance(Two(input, i)))
                        } else {
                            null
                        }
                    }
                }
                .collect(Collectors.toList()) as List<ItemWithDistance<OutputType>>
        val aggregated = schema.predictionAggregator(predictions)
        return aggregated
    }

}
