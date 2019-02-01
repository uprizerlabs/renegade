package renegade.supervised

import renegade.aggregators.ClassificationCounter
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.indexes.bucketing.ItemBucketer
import java.util.concurrent.ConcurrentHashMap

fun <InputType : Any, OutputType : Any> buildFastClassifier(
        trainingData: Collection<Pair<InputType, OutputType>>,
        distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>,
        bits: Int = 8
): FastClassifier<InputType, OutputType> {

    val distFunc = buildDistanceFunction(distanceModelBuilders, trainingData.toList())
    val itemBucketer = ItemBucketer(distFunc, trainingData.map {it.first}, bits)
    val buckets = ConcurrentHashMap<Any, ClassificationCounter<OutputType>>()
    trainingData.parallelStream().forEach {
        buckets.computeIfAbsent(itemBucketer.bucket(it.first)) { ClassificationCounter() }.plusAssign(it.second)
    }
    return FastClassifier(itemBucketer, buckets)
}

class FastClassifier<InputType : Any, OutputType : Any>(
        private val itemBucketer: ItemBucketer<InputType, Double>,
        private val buckets: ConcurrentHashMap<Any, ClassificationCounter<OutputType>>
) : Classifier<InputType, OutputType> {

    override fun predict(input: InputType): Map<OutputType, Double> {
        val bucket = itemBucketer.bucket(input)
        val classificationCounter = buckets[bucket]
        return classificationCounter?.toProbabilityMap() ?: emptyMap()
    }
}