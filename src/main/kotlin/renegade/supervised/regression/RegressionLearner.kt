package renegade.supervised.regression

import org.apache.commons.math3.stat.regression.SimpleRegression
import renegade.Distance
import renegade.aggregators.ItemWithDistance
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.opt.OptConfig
import renegade.supervised.LearnedModel
import renegade.supervised.Learner
import renegade.supervised.VertexPointLearner
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class RegressionLearner<InputType : Any>(
        cfg: OptConfig,
        trainingData: List<Pair<InputType, Double>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>,
        regressionStrategy: RegressionStrategy = RegressionStrategy.Mean
) : Learner<InputType, Double, RegressionLearner.Prediction>(cfg, { a, b -> abs(a - b)}, this::regressionAggregator, regressionStrategy::error) {

    companion object {
        fun regressionAggregator(items: Collection<ItemWithDistance<Double>>): Prediction {
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

    }

    private val vertexPointLearner: VertexPointLearner<InputType, Distance, Prediction>

    init {
        vertexPointLearner = VertexPointLearner(
                cfg = cfg,
                trainingData = trainingData,
                distanceModelBuilders = distanceModelBuilders,
                outputDistance = { a, b -> abs(a - b) },
                predictionAggregator = ::regressionAggregator,
                predictionError = regressionStrategy::error
        )
    }

    fun predict(input: InputType) = vertexPointLearner.predict(input)

    data class Prediction(val mean: Double, val extrapolated: Double, val summary: DoubleSummaryStatistics, val simpleReg: SimpleRegression)

    override fun learn(data: List<Pair<InputType, Double>>, distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>): LearnedModel<InputType, Double, Prediction> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

