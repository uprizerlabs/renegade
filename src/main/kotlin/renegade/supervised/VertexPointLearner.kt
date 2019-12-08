package renegade.supervised

import com.eatthepath.jvptree.VPTree
import com.google.common.base.Stopwatch
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import mu.KotlinLogging
import renegade.Distance
import renegade.MetricSpace
import renegade.aggregators.ItemWithDistance
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.opt.IntRangeParameter
import renegade.opt.OptConfig
import renegade.opt.ValueListParameter
import renegade.supervised.VertexPointLearner.Parameters.distanceCacheSize
import renegade.supervised.VertexPointLearner.Parameters.sampleSize
import renegade.util.Two
import renegade.util.math.sqrt
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.math.min


private val logger = KotlinLogging.logger {}

class VertexPointLearner<InputType : Any, OutputType : Any, PredictionType : Any>(
        val cfg: OptConfig,
        trainingData: List<Pair<InputType, OutputType>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>,
        outputDistance: (OutputType, OutputType) -> Distance,
        val metricSpace: MetricSpace<InputType, OutputType> = run {
            logger.info("No metric space supplied, building...")
            MetricSpace(cfg, modelBuilders = distanceModelBuilders, trainingData = trainingData, outputDistance = outputDistance)
        },
        private val predictionAggregator: (Collection<ItemWithDistance<OutputType>>) -> PredictionType,
        private val predictionError: (OutputType, PredictionType) -> Double
) {
    private val distanceCache: LoadingCache<Two<InputType>, Distance> = CacheBuilder.newBuilder()
            .maximumSize(cfg[distanceCacheSize].toLong())
            .build(object : CacheLoader<Two<InputType>, Distance>() {
                override fun load(key: Two<InputType>) = metricSpace.estimateDistance(key)
            })

    private val vpIndex: VPTree<Pair<InputType, OutputType?>, Pair<InputType, OutputType?>> = run {
        logger.info("Building vpIndex with ${trainingData.size} training instances")
        val vpt = VPTree<Pair<InputType, OutputType?>, Pair<InputType, OutputType?>> { a, b -> distanceCache[Two(a.first, b.first)] }
        vpt.addAll(trainingData)
        logger.info("vpIndex built.  distanceCache size:${distanceCache.size()}")
        vpt
    }

    object Parameters {
        val distanceCacheSize = IntRangeParameter("distanceCacheSize", 1_000..1_000_000, 1_000_000)
        val sampleSize = IntRangeParameter("sampleSize", 100_000..1_000_000, 60_000)
        val multithreadDistance = ValueListParameter("multithreadDistance", true, false)
    }

    val insetSize: Int by lazy {
        val sampleSize = min(cfg[sampleSize], trainingData.size)
        logger.info("Measuring optimal insetSize with sampleSize $sampleSize")
        val samples = trainingData.shuffled().subList(0, sampleSize)

        logger.info("Dumping insetSize experimental data")
        println("size\tRMSE\ttime(s)")
        data class Best(val insetSize: Int, val rmse: Double)

        var bestSoFar: Best? = null
        for (testInsetSize in 2..(sampleSize / 2)) {
            val sw = Stopwatch.createStarted()
            val rmse = samples.map { (i, o) ->
                val prediction = predict(i, testInsetSize)
                val error = predictionError(o, prediction)
                error * error
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
        logger.info("Cache stats after insetTest: ${distanceCache.stats()}")
        bestSoFar!!.insetSize
    }

    fun predict(input: InputType, insetSize: Int): PredictionType {

        val predictions = vpIndex.getNearestNeighbors((input to null), insetSize)
                .filterNot { it.first == input }.let {
                    if (cfg[Parameters.multithreadDistance]) {
                        it.parallelStream()
                    } else {
                        it.stream()
                    }.map { (i, o) ->
                        if (o != null) {
                            val distance = distanceCache[Two(input, i)]
                            ItemWithDistance(o, distance)
                        } else {
                            null
                        }
                    }
                }
                .collect(Collectors.toList()) as List<ItemWithDistance<OutputType>>
        val aggregated = predictionAggregator(predictions)
        return aggregated
    }

    fun predict(input: InputType): PredictionType {
        return predict(input, insetSize)

    }

}
