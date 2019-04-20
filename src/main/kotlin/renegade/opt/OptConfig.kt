package renegade.opt

import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

class OptConfig : Serializable {

    val options = ConcurrentHashMap<String, Any>()

    internal operator fun <T : Any> set(param: OptimizableParameter<T>, v: T) {
        options[param.label] = v as Any
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(param: OptimizableParameter<T>): T =
        (options[param.label] as T?) ?: run {
            param.default.let { r ->
                options[param.label] = r
                r
            }
        }



    override fun toString(): String = options.toString()
}