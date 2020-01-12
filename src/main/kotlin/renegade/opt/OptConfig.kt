package renegade.opt

import com.github.salomonbrys.kotson.set
import com.google.gson.*
import java.io.Serializable
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

enum class CreationStrategy {
    dafault, randomSample
}

class OptConfig(val creationStrategy: CreationStrategy = CreationStrategy.dafault) : Serializable {

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

class OptConfigTypeAdaptor : JsonSerializer<OptConfig>, JsonDeserializer<OptConfig> {
    override fun serialize(src: OptConfig, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val configObject = JsonObject()

        JsonObject().let { optionsObject ->
            for ((label, value) in src.options) {
                JsonObject().let { valueObject ->
                    valueObject["type"] = value::class.qualifiedName
                    valueObject["value"] = context.serialize(value)
                    optionsObject[label] = valueObject
                }
            }
            configObject["options"] = optionsObject
        }

        JsonObject().let { parametersObject ->
            src.parameters.let { parameters ->
                if (parameters != null) {
                    for ((label, op) in parameters) {
                        JsonObject().let { parameterObject ->
                            parameterObject["type"] = op::class.qualifiedName
                            parameterObject["value"] = context.serialize(op)
                            parametersObject[label] = parameterObject
                        }
                    }
                    configObject["parameters"] = parametersObject
                }
            }
        }
        return configObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OptConfig {
        val optConfig = OptConfig()
        for ((label, valueObject) in json.asJsonObject["options"].asJsonObject.entrySet()) {
            if (valueObject is JsonObject) {
                val type = valueObject["type"].asString
                val cls = when (type) {
                    "kotlin.Double" -> Double::class.java
                    "kotlin.Int" -> Int::class.java
                    else -> Class.forName(type)
                }
                val value = context.deserialize<Any>(valueObject["value"], cls)
                optConfig.options[label] = value
            } else {
                error("Expecting a JsonObject, got a ${valueObject::class}")
            }
        }

        for ((label, parameterObject) in json.asJsonObject["parameters"].asJsonObject.entrySet()) {
            optConfig.parameters.let { parameters ->
                if (parameterObject is JsonObject && parameters != null) {
                    val type = parameterObject["type"].asString
                    val cls = Class.forName(type)
                    val op = context.deserialize<OptimizableParameter<*>>(parameterObject["value"], cls)
                    parameters[label] = op
                } else {
                    error("Expecting a JsonObject, got a ${parameterObject::class}")
                }
            }
        }

        return optConfig
    }

}