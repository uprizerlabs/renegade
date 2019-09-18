package renegade.opt

import com.fatboyindustrial.gsonjavatime.Converters
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import com.github.salomonbrys.kotson.*
import com.google.gson.*
import renegade.util.KClassTypeAdaptor
import kotlin.reflect.KClass

class Optimizer(
        val logFile : Path,
        val goal : Goal,
        toOptimize : (OptConfig) -> Double,
        randomSearchThreshold : Int = 20
) {
    companion object {
        val gson = Converters.registerInstant(GsonBuilder())
                .registerTypeAdapter(OptConfig::class.java, OptConfigTypeAdaptor())
                .registerTypeAdapter(KClass::class.java, KClassTypeAdaptor())
                .create()
    }

    @Volatile
    private var hasWrittenParameters = false

    private val log = Files.readAllLines(logFile).map {
        gson.fromJson<OptimizationRun>(it)
    }.toMutableList()

    @Volatile
    private var parameters = log.mapNotNull { it.config.parameters }.lastOrNull()

    init {

    }

    private fun generateOptConfig() : OptConfig {
        val cfg = OptConfig()
        parameters.let { param ->
            if (param != null) {
                for (p in param.values) {
                    cfg.options[p.label] = p.randomSample()
                }
            }
        }
        return cfg
    }

    private fun logRun(or : OptimizationRun) {
        or.config.parameters.let { p ->
            if (p != null) {
                parameters = p
            }
        }
        if (hasWrittenParameters) {
            or.config.parameters = null
        }
        logFile.toFile().appendText(gson.toJson(or)+"\n")
        log += or
        hasWrittenParameters = true
    }

    enum class Goal {
        MAXIMIZE, MINIMIZE
    }

    data class OptimizationRun(val time : Instant, val config : OptConfig, val score : Double)
}
