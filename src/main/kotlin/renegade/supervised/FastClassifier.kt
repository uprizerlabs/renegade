package renegade.supervised

import renegade.aggregators.ClassificationCounter
import renegade.distanceModelBuilder.DistanceModelBuilder
import renegade.indexes.bucketing.ItemBucketer
import renegade.opt.*
import java.util.concurrent.ConcurrentHashMap

val FAST_CLASSIFIER_BITS = IntRangeParameter("fast-classifier-bits", 3 .. 16, 10)

fun <InputType : Any, OutputType : Any> buildFastClassifier(cfg : OptConfig,
                                                            trainingData: Collection<Pair<InputType, OutputType>>,
                                                            distanceModelBuilders: ArrayList<DistanceModelBuilder<InputType>>
): FastClassifier<InputType, OutputType> {

    val distFunc = buildDistanceFunction(cfg, distanceModelBuilders, trainingData.toList())
    val itemBucketer = ItemBucketer(distFunc, trainingData.map {it.first}, cfg[FAST_CLASSIFIER_BITS])
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