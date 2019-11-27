package renegade.supervised.vp

import com.eatthepath.jvptree.VPTree
import com.google.common.base.Stopwatch
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.nustaq.serialization.annotations.Predict
import renegade.Distance
import renegade.MetricSpace
import renegade.aggregators.ItemWithDistance
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.opt.IntRangeParameter
import renegade.opt.OptConfig
import renegade.util.Two
import renegade.util.math.sqrt
import java.util.Collections.max
import java.util.concurrent.TimeUnit


private val logger = KotlinLogging.logger {}

val distanceCacheSize = IntRangeParameter("distanceCacheSize", 1_000..100_000, 10_000)

class VPPredictor<InputType : Any, OutputType : Any, PredictionType : Any>(
        cfg: OptConfig,
        trainingData: List<Pair<InputType, OutputType>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>,
        outputDistance: (OutputType, OutputType) -> Distance,
        val metricSpace: MetricSpace<InputType, OutputType> = run {
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
        val vpt = VPTree<Pair<InputType, OutputType?>, Pair<InputType, OutputType?>> { a, b -> distanceCache[Two(a.first, b.first)] }
        vpt.addAll(trainingData)
        vpt
    }

    val insetSize: Int by lazy {
        val sampleSize = trainingData.size
        logger.info("Measuring optimal insetSize with sampleSize $sampleSize")
        val samples = trainingData.shuffled().subList(0, sampleSize)

        logger.info("Dumping insetSize experimental data")
        println("size\tRMSE\ttime(s)")
        (5..50).map { testInsetSize ->
            val sw = Stopwatch.createStarted()
            val rmse = samples.map { (i, o) ->
                val prediction = predict(i, testInsetSize)
                val error =  predictionError(o, prediction)
                error * error
            }.average().sqrt
            println("$testInsetSize\t$rmse\t${sw.elapsed(TimeUnit.MILLISECONDS).toDouble() / 1000.0}")
        }

        5 // TODO: FIX
    }

    fun predict(input: InputType, insetSize: Int): PredictionType {

        val predictions = vpIndex.getNearestNeighbors((input to null), insetSize)
                .filterNot { it.first == input }
                .mapNotNull { (i, o) ->
                    if (o != null) {
                        val distance = distanceCache[Two(input, i)]
                        ItemWithDistance(o, distance)
                    } else {
                        null
                    }
                }
        return predictionAggregator(predictions)
    }

    fun predict(input: InputType): PredictionType {
        return predict(input, insetSize)

    }

}
