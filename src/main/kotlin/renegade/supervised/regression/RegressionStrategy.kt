package renegade.supervised.regression

import renegade.supervised.regression.RegressionLearner.Prediction
import kotlin.math.abs

interface RegressionStrategy {
    fun error(target: Double, prediction: Prediction): Double

    object Mean : RegressionStrategy {
        override fun error(target: Double, prediction: Prediction): Double {
            return abs(target - prediction.mean)
        }
    }

    object Extrapolated : RegressionStrategy {
        override fun error(target: Double, prediction: Prediction): Double {
            return abs(target - prediction.extrapolated)
        }
    }
}