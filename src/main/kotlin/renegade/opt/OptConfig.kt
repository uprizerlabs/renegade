package renegade.opt

import java.util.concurrent.ConcurrentHashMap

enum class CreationStrategy {
    dafault, randomSample
}

class OptConfig(val creationStrategy: CreationStrategy = CreationStrategy.dafault)  {

    val options = ConcurrentHashMap<String, Any>()

    @Volatile var parameters : ConcurrentHashMap<String, OptimizableParameter<*>>? = ConcurrentHashMap()

    operator fun <T : Any> set(param: OptimizableParameter<*>, v: T) {
        parameters?.set(param.label, param)
        options[param.label] = v as Any
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(param: OptimizableParameter<T>): T {
        parameters?.set(param.label, param)
        return (options[param.label] as T?) ?: run {
            when (creationStrategy) {
                CreationStrategy.dafault -> param.default.let { r ->
                    options[param.label] = r
                    r
                }
                CreationStrategy.randomSample -> param.randomSample().let { r ->
                    options[param.label] = r
                    r
                }
            }
        }
    }

    override fun toString(): String = options.toString()
}

