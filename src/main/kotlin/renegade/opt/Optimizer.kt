package renegade.opt

import java.nio.file.Files
import java.nio.file.Path
import java.io.PrintStream
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class Optimizer(private val toLoss: (Score) -> Loss, private val log: OptimizerLog, private val isDone: (Score, Int) -> Boolean) {

    fun optimize(func: (OptConfig) -> Score) : OptConfig {

        var count = 0

        do {
            val parametersByLabel = HashMap<String, OptimizableParameter<*>>()
            val valuesByParameter = HashMap<String, MutableMap<Any, MutableList<Loss>>>()

            for (l in log.read()) {
                for ((label, value) in l.cfg.options) {
                    valuesByParameter.computeIfAbsent(label) { HashMap() }.computeIfAbsent(value) { ArrayList() }.add(toLoss(l.score))
                    parametersByLabel.putIfAbsent(label, l.cfg.parameters?.get(label) ?: error("Can't find $label"))
                }
            }

            val cfg = OptConfig()

            for (knownParameter in parametersByLabel.values) {
                val history = valuesByParameter[knownParameter.label]
                if (history != null) {
                    val v = selectNext(knownParameter, history)
                    cfg[knownParameter] = v
                }
            }

            val score = func(cfg)

            log.write(OptScore(System.currentTimeMillis(), cfg, score))

        } while (!isDone(score, count++))

        return log.read().minByOrNull { toLoss(it.score) }!!.cfg
    }

}

interface OptimizerLog {
    fun write(optScore: OptScore)

    fun read(): List<OptScore>
}

class MemoryOptimizerLog : OptimizerLog {
    private val logList = CopyOnWriteArrayList<OptScore>()

    override fun write(optScore: OptScore) {
        logList += optScore
    }

    override fun read(): List<OptScore> = logList

}
/*
class FileOptimizerLog(private val path: Path) : OptimizerLog {

    private val gson = GsonBuilder().registerTypeAdapter(OptConfig::class.java, OptConfigTypeAdaptor()).create()

    override fun write(optScore: OptScore) {
        PrintStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)).use { ps ->
            ps.println(gson.toJson(optScore))
        }
    }

    override fun read(): List<OptScore>{
        return if (Files.exists(path)) {
            Files.readAllLines(path)
                    .map { gson.fromJson(it, OptScore::class.java) }
        } else {
            emptyList()
        }
    }

}

 */

class Score : MutableMap<String, Double> by ConcurrentHashMap() {
    override fun toString(): String {
        return "Score("+this.entries.map {  "${it.key} = ${it.value}" }.joinToString(separator = ",")+")"
    }
}

data class OptScore(val time : Long, val cfg: OptConfig, val score: Score)