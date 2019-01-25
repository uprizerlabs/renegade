package renegade.distanceModelBuilder.inputTypes.metric

import renegade.distanceModelBuilder.*
import renegade.supervised.Regressor
import renegade.util.Two
import kotlin.math.*

class AdvancedDoubleDistanceModelBuilder(override val label: String) : DistanceModelBuilder<Double>(label) {
    override fun build(inputDistances: InputDistances<Double>): AdvancedDoubleDistanceModel {

        val distanceModelBuilders = ArrayList<DistanceModelBuilder<Two<Double>>>()

        distanceModelBuilders += DoubleDistanceModelBuilder("min-dist").map { min(it.first, it.second) }

        distanceModelBuilders += DoubleDistanceModelBuilder("max-dist").map { max(it.first, it.second) }

       // distanceModelBuilders += DoubleDistanceModelBuilder("avg-dist").map { listOf(it.first, it.second).average() }

        val regressor = Regressor(inputDistances.map { it.inputs to it.dist }, distanceModelBuilders)

        return AdvancedDoubleDistanceModel(label, regressor)
    }
}

class AdvancedDoubleDistanceModel(label: String, val regressor: Regressor<Two<Double>>) : DistanceModel<Double>(label, { two ->
    regressor.predict(two).value
})