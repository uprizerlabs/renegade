package renegade.opt

class Optimizer {
    fun createConfig(context : Map<String, Any>) : OptConfig {
        TODO()
    }

    fun reportConfigOutcome(config : OptConfig, targetMetrics : Map<String, Double>) {

    }
}

data class ConfigurationRun(val context : Map<String, Any>, val config : Map<OptimizableParameter<*>, Any>, val outcome : Map<String, Double>)