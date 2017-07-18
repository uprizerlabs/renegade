package dnn.distanceModelBuilder.inputTypes

import dnn.distanceModelBuilder.*
import dnn.util.AveragingAccumulator

class IdentityDistanceModelBuilder(label : String? = null) : DistanceModelBuilder<Any>(label) {
    override fun build(trainingData: InputDistances<Any>) : DistanceModel<Any> {
            val pairMap = trainingData
                    .groupingBy { it.inputs }
                    .fold(AveragingAccumulator(), { accumulator, element -> accumulator + element.dist })

            val globalSum = pairMap.values.map { it.sum }.sum()
            val globalCount = pairMap.values.map { it.count }.sum()

            return DistanceModel { pair -> pairMap[pair]?.avg ?: globalSum / globalCount }
    }

}