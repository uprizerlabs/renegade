package renegade.supervised.vp

import com.eatthepath.jvptree.VPTree
import com.google.common.base.Stopwatch
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import mu.KotlinLogging
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import org.apache.commons.math3.stat.regression.SimpleRegression
import renegade.MetricSpace
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.opt.OptConfig
import renegade.util.Two
import renegade.util.math.sqrt
import java.util.concurrent.TimeUnit
import kotlin.math.abs


private val logger = KotlinLogging.logger {}

class VPRegressor<InputType : Any>(
        cfg: OptConfig,
        trainingData: List<Pair<InputType, Double>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>) {
    private val metricSpace: MetricSpace<InputType, Double> = run {
        MetricSpace(cfg, modelBuilders = distanceModelBuilders, trainingData = trainingData, outputDistance = { a, b -> abs(a - b) })
    }

    private val cache: LoadingCache<Two<InputType>, Double> = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .build(object : CacheLoader<Two<InputType>, Double>() {
                override fun load(key: Two<InputType>) = metricSpace.estimateDistance(key)
            })

    private val vpIndex: VPTree<Pair<InputType, Double?>, Pair<InputType, Double?>> = run {
        val vpt = VPTree<Pair<InputType, Double?>, Pair<InputType, Double?>> { a, b -> cache[Two(a.first, b.first)] }
        vpt.addAll(trainingData)
        vpt
    }

    val insetSize : Int by lazy {
        val sampleSize = trainingData.size
        logger.info("Measuring optimal insetSize with sampleSize $sampleSize")
        val samples = trainingData.shuffled().subList(0, sampleSize)

        logger.info("Dumping insetSize experimental data")
        println("size\tRMSE\ttime(s)")
        (5 .. 50).map { testInsetSize ->
            val sw = Stopwatch.createStarted()
            val rmse = samples.map { (i, o) ->
                val prediction = predict(i, testInsetSize, false)
                val error = Math.abs(prediction - o)
                error*error
            }.average().sqrt
            println("$testInsetSize\t$rmse\t${sw.elapsed(TimeUnit.MILLISECONDS).toDouble()/1000.0}")
        }

        5 // TODO: FIX
    }

    fun predict(input : InputType, insetSize : Int, extrapolate : Boolean) : Double {
        val reg = SimpleRegression(true)
        val avg = SummaryStatistics()
        vpIndex.getNearestNeighbors((input to null), insetSize)
                .filterNot { it.first == input }
                .forEach { (i, o) -> if (o != null) {
                    val distance = cache[Two(input, i)]
                    reg.addData(distance, o)
                    avg.addValue(o)
                } }
        return if (extrapolate) reg.intercept else avg.mean
    }

    fun predict(input: InputType, extrapolate: Boolean = true): Double {
        return predict(input, insetSize, extrapolate)

    }

}