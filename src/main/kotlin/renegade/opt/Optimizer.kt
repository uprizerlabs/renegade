package renegade.opt

import com.fatboyindustrial.gsonjavatime.Converters
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import com.github.salomonbrys.kotson.*
import com.google.gson.*
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory


private val gson = Converters.registerInstant(GsonBuilder())
        .registerTypeAdapter(OptConfig::class.java, OptConfigTypeAdaptor())
        .create()

class Optimizer(
        val logFile : Path,
        val goal : Goal,
        toOptimize : (OptConfig) -> Double,
        randomSearchThreshold : Int = 20
) {
    private val log = Files.readAllLines(logFile).map {
        gson.fromJson<OptimizationRun>(it)
    }.toMutableList()

    private val parameters = HashSet<OptimizableParameter<*>>()

    init {

    }

    private fun checkForNewParameters(or : OptimizationRun) {

    }

    private fun logRun(or : OptimizationRun) {
        logFile.toFile().appendText(gson.toJson(or)+"\n")
        log += or
    }

    enum class Goal {
        MAXIMIZE, MINIMIZE
    }

    data class OptimizationRun(val time : Instant, val config : OptConfig, val score : Double)
}
