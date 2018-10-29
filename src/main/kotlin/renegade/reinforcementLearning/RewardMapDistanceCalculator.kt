package renegade.reinforcementLearning

import java.lang.Math.abs
import java.time.*
import java.util.*

class RewardMapDistanceCalculator(private val timeCutoff: Instant) : (TreeMap<Instant, List<Double>>, TreeMap<Instant, List<Double>>) -> Double {
    override fun invoke(aEvents: TreeMap<Instant, List<Double>>, bEvents: TreeMap<Instant, List<Double>>): Double {

        val firstEventA = aEvents.keys.first()
        val firstEventB = bEvents.keys.first()
        val latest = when {
            firstEventA.isBefore(firstEventB) -> firstEventB
            else -> firstEventA
        }
        val duration = Duration.between(latest, timeCutoff)
        val aRewardSum = aEvents.headMap(firstEventA + duration).values.flatten().sum()
        val bRewardSum = bEvents.headMap(firstEventB + duration).values.flatten().sum()
        return if (aRewardSum + bRewardSum > 0) {
            abs(aRewardSum - bRewardSum) / (aRewardSum + bRewardSum)
        } else 0.0
    }
}

