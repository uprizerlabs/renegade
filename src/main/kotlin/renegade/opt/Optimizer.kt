package renegade.opt

class Optimizer {
    fun createConfig(context : Map<String, Any>) : OptConfig {
        TODO()
    }

    fun reportConfigOutcome(config : OptConfig, targetMetrics : Map<String, Double>) {

    }
}

class OptConfig(val expectedMetrics : Map<String, Double>?) {

    private val parameterValues = HashMap<OptimizableParameter<*>, Any>()

    internal operator fun <T : Any> set(param : OptimizableParameter<T>, v : T) {
        parameterValues[param] = v as Any
    }

    operator fun <T : Any> get(param : OptimizableParameter<T>) : T {
        if (param is ValueListParameter<T>) {
            return (parameterValues[param] as T?) ?: run {
                param.values.random().let { r ->
                    parameterValues[param] = r
                    r
                }
            }
        } else {
            throw UnsupportedOperationException("Currently we only handle ValueListParameters")
        }
    }
}

data class ConfigurationRun(val context : Map<String, Any>, val config : Map<OptimizableParameter<*>, Any>, val outcome : Map<String, Double>)