package renegade.supervised.vp

import org.apache.commons.math3.stat.regression.SimpleRegression
import renegade.Distance
import renegade.aggregators.ItemWithDistance
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.opt.OptConfig
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class Regression {
    fun regressionAggregator(items : Collection<ItemWithDistance<Distance>>) : RegressionPrediction {
        val summary = DoubleSummaryStatistics()
        val simpleRegression = SimpleRegression(true)

        for (item in items) {
            summary.accept(item.item)
            simpleRegression.addData(item.distance, item.item)
        }

        return RegressionPrediction(
                mean = summary.average,
                extrapolated = simpleRegression.intercept,
                summary = summary,
                simpleReg = simpleRegression
        )
    }

    interface RegressionStrategy {
        fun error(target : Double, prediction : RegressionPrediction) : Double

        object Mean : RegressionStrategy {
            override fun error(target: Double, prediction: RegressionPrediction): Double {
                return abs(target - prediction.mean)
            }
        }

        object Extrapolated : RegressionStrategy {
            override fun error(target: Double, prediction: RegressionPrediction): Double {
                return abs(target - prediction.extrapolated)
            }
        }
    }

    fun <InputType : Any> createRegressor(cfg: OptConfig, trainingData: List<Pair<InputType, Double>>, distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>, regressionStrategy: RegressionStrategy = RegressionStrategy.Mean) {
        val vpPredictor = VPPredictor<InputType, Double, RegressionPrediction>(
                cfg = cfg,
                trainingData = trainingData,
                distanceModelBuilders = distanceModelBuilders,
                outputDistance = { a, b -> abs(a-b)},
                predictionAggregator = ::regressionAggregator,
                predictionError = regressionStrategy::error
        )
    }
}

data class RegressionPrediction(val mean : Double, val extrapolated : Double, val summary : DoubleSummaryStatistics, val simpleReg : SimpleRegression)