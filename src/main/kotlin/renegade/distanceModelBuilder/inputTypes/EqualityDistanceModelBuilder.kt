package renegade.distanceModelBuilder.inputTypes

/*
class EqualityDistanceModelBuilder(override val label : String? = null) : DistanceModelBuilder<Any>(label) {
    override fun build(trainingData: InputDistances<Any>) : DistanceModel<Any> {
        val pairMap = trainingData
                .groupingBy { it.inputs.first == it.inputs.second }
                .fold(AveragingAccumulator(), { accumulator, element -> accumulator + element.dist })

        return DistanceModel { pair -> pairMap[pair.first == pair.second]?.avg ?: 0.0 }
    }
}
        */