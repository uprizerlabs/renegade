package renegade.supervised.regression

import org.apache.commons.math3.stat.regression.SimpleRegression
import renegade.Distance
import renegade.aggregators.ItemWithDistance
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.opt.OptConfig
import renegade.supervised.VertexPointSupervisedLearner
import java.util.*
import kotlin.math.abs

class Regression<InputType : Any>(
        cfg: OptConfig,
        trainingData: List<Pair<InputType, Double>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>,
        regressionStrategy: RegressionStrategy = RegressionStrategy.Mean
) {

    private val vertexPointSupervisedLearner: VertexPointSupervisedLearner<InputType, Distance, Prediction>

    init {
        vertexPointSupervisedLearner = VertexPointSupervisedLearner(
                cfg = cfg,
                trainingData = trainingData,
                distanceModelBuilders = distanceModelBuilders,
                outputDistance = { a, b -> abs(a - b) },
                predictionAggregator = ::regressionAggregator,
                predictionError = regressionStrategy::error
        )
    }

    fun predict(input: InputType) = vertexPointSupervisedLearner.predict(input)

    private fun regressionAggregator(items: Collection<ItemWithDistance<Double>>): Prediction {
        val summary = DoubleSummaryStatistics()
        val simpleRegression = SimpleRegression(true)

        for (item in items) {
            summary.accept(item.item)
            simpleRegression.addData(item.distance, item.item)
        }

        return Prediction(
                mean = summary.average,
                extrapolated = simpleRegression.intercept,
                summary = summary,
                simpleReg = simpleRegression
        )
    }

    data class Prediction(val mean: Double, val extrapolated: Double, val summary: DoubleSummaryStatistics, val simpleReg: SimpleRegression)
}

