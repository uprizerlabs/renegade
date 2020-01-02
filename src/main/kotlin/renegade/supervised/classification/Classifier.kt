package renegade.supervised.classification

import mu.KotlinLogging
import renegade.MetricSpace
import renegade.aggregators.ItemWithDistance
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.distanceModelBuilder.inputTypes.metric.DoubleDistanceModelBuilder
import renegade.opt.OptConfig
import renegade.supervised.Schemas
import renegade.supervised.VertexPointLearner
import renegade.supervised.WaypointLearner

private val logger = KotlinLogging.logger {}

fun main() {
    val schema = Schemas.RegressionSchema<List<Double>>(false)

/*    val data = loadM

    val wl = WaypointLearner(OptConfig(), schema)

    val dmb = listOf<DistanceModelBuilder<List<Double>>>(DoubleDistanceModelBuilder().map {it[0]})

    val metric = MetricSpace(OptConfig(), dmb, listOf(), null, schema.outputDistance)

    val model = wl.learn(metric, listOf())
*/

}
