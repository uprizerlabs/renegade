package dnn

import com.google.common.collect.Multimap
import dnn.distance.*
import dnn.relevance.perAttribute.*
import dnn.util.*
import mu.KotlinLogging
import dnn.relevance.RelevanceMeasure as Measure

/**
 * Created by ian on 7/3/17.
 */

typealias Attributes<AttributeKeyType> = Map<AttributeKeyType, Any>
typealias RelevanceMeasureFactory = (Multimap<Two<Any>, Double>) -> Measure<*>
typealias MeasuresByLabel = Map<String, Measure<*>>
typealias RelevanceFactories = Map<String, RelevanceMeasureFactory>

private val logger = KotlinLogging.logger {}


class MetricSpaceRefiner<AttributeKeyType : Any, TargetType : Any>(
        val attributesWithTarget: Map<Attributes<AttributeKeyType>, TargetType>,
        val maxPairSamples: Int = Math.min(1_000_000, attributesWithTarget.size.sqr / 2).toInt(),
        val maxCategories: Int = 10,
        val targetRelevance: (TargetType, TargetType) -> Double = { a, b -> if (a == b) 1.0 else 0.0 }
) {
    private val relevanceByAttributes = buildRelevancePairs(attributesWithTarget)
    private val valueRelevanceByAttr = relevanceByAttributes.extractAttrValuesByKey(attributesWithTarget)
    private val measureFactories = createRelevanceMeasureFactories()
    private fun createInitialMeasures() : Map<AttributeKeyType, MeasuresByLabel> {
        val measures = measureFactories.innerJoin(valueRelevanceByAttr).mapValues { (_, measuresRelevance) ->
            val (measures, relevance) = measuresRelevance
            measures.mapValues { (_, measureFactory) ->
                measureFactory.invoke(relevance)
            }
        }
        return measures
    }

    @Suppress("UNCHECKED_CAST")
    private fun createRelevanceMeasureFactories(): Map<AttributeKeyType, RelevanceFactories> {
        return valueRelevanceByAttr.mapValues { (_, relevanceByPair) ->
            val measures
                    = HashMap<String, RelevanceMeasureFactory>().noOverwrite()
            val samples = relevanceByPair.entries().take(100).map { it.key.first }
            val isNumeric = samples.all { it is Number }
            if (isNumeric) {
                val numberSamples = samples as Iterable<Number>
                measures["simple-dist"] = { RelevanceFromDistance(it as Multimap<Two<Number>, Double>, ::simpleNumberMeasure) }
                measures["quantile-dist"] = { RelevanceFromDistance(it as Multimap<Two<Number>, Double>, QuantizedNumberMeasure(numberSamples)::distance) }
            }
            val distinctValues = samples.distinct().size
            if (distinctValues <= maxCategories) {
                measures["category"] = { RelevanceFromCategories(it) }
            }
            measures
        }
    }

    private fun Multimap<Two<Attributes<AttributeKeyType>>, Double>.extractAttrValuesByKey(trainingData: Map<Attributes<AttributeKeyType>, TargetType>)
            : Map<AttributeKeyType, Multimap<Two<Any>, Double>> {
        val attributeKeys = trainingData.keys.take(100).flatMap { it.keys }.toSet()

        val relevancePairsByAttrKey: Map<AttributeKeyType, Multimap<Two<Any>, Double>> =
                this.breakIntoTriples(attributeKeys).entries()
                        .groupBy { it.key }
                        .mapValues {
                            it.value.groupBy { (_, second) ->
                                second.first
                            }.mapValues { (_, v) -> v.map { it.value.second } }.toMultimap()
                        }
        return relevancePairsByAttrKey
    }

    private fun Multimap<Two<Attributes<AttributeKeyType>>, Double>.breakIntoTriples(attributeKeys: Set<AttributeKeyType>):
            Multimap<AttributeKeyType, Pair<Two<Any>, Double>> {
        return attributeKeys.map { attributeKey ->
            this.entries().mapNotNull { (attributesPair, relevance) ->
                val firstValue = attributesPair.first[attributeKey]
                val secondValue = attributesPair.second[attributeKey]
                if (firstValue != null && secondValue != null) {
                    attributeKey to (Two(firstValue, secondValue) to relevance)
                } else {
                    null
                }
            }
        }.flatten().toMultimap()
    }

    private fun buildRelevancePairs(trainingData: Map<Attributes<AttributeKeyType>, TargetType>): Multimap<Two<Attributes<AttributeKeyType>>, Double> {
        return trainingData
                .entries
                .toList()
                .randomDistinctPairs
                .take(maxPairSamples)
                .groupBy { Two(it.first.key, it.second.key) }
                .mapValues { (_, v) ->
                    v.map { n: Two<Map.Entry<Attributes<AttributeKeyType>, TargetType>> ->
                        targetRelevance(n.first.value, n.second.value)
                    }
                }.toMultimap()
    }
}

