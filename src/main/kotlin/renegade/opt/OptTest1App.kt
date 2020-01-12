package renegade.opt

import com.google.common.base.Stopwatch
import mu.KotlinLogging
import renegade.MetricSpace
import renegade.datasets.mnist.createMnistBuilders
import renegade.datasets.mnist.loadMnistDataset
import renegade.supervised.Schemas
import renegade.supervised.VertexPointLearner
import renegade.supervised.WaypointLearner
import renegade.util.math.sqr
import renegade.util.math.sqrt
import renegade.util.splitTrainTest
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

fun main() {
    val log = FileOptimizerLog(Paths.get("classifier-opt-2.jsonp"))

    val optimizer = Optimizer({it["rmse"]!!}, log, {s, i -> i > 100})

    val sw = Stopwatch.createStarted()

    val data = loadMnistDataset().map { Pair(it.first.map { it.toDouble() }, it.second) }.splitTrainTest(0.9)

    val schema = Schemas.ClassifierSchema<List<Double>, Int>()

    val metricCfg = OptConfig()

    metricCfg[MetricSpace.Parameters.maxIterations] = 20
    metricCfg[MetricSpace.Parameters.learningRate] = 0.1
    metricCfg[MetricSpace.Parameters.maxModelCount] = 1000
    metricCfg[MetricSpace.Parameters.maxSamples] = 1_000_000

    val metric = MetricSpace(metricCfg, createMnistBuilders(data.train), data.train) { a, b -> if ( a == b) 0.0 else 1.0 }

    val learnerType = ValueListParameter("learnerType", *LearnerType.values())

    val optimum = optimizer.optimize { cfg ->
        logger.info("TRYING $cfg")

        val sw = Stopwatch.createStarted()

        val learner = when(cfg[learnerType]) {
            LearnerType.WAYPOINT -> WaypointLearner(cfg, schema)
            LearnerType.VERTEX_POINT -> VertexPointLearner(cfg, schema)
        }

        val model = learner.learn(metric, data.train)

        val error = DoubleSummaryStatistics()
        for (datum in data.test) {
            val prediction = model.predict(datum.first)
            val e = (1.0 - (prediction.getOrDefault(datum.second, 0.0))).sqr
            error.accept(e)
        }

        val score = Score()

        score["rmse"] = error.average.sqrt
        score["time"] = sw.elapsed(TimeUnit.MILLISECONDS).toDouble()

        logger.info("RESULT $cfg\t$score")

        score
    }

}



enum class LearnerType {
    WAYPOINT, VERTEX_POINT
}