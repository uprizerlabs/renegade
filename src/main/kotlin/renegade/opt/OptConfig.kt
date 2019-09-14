package renegade.opt

import com.github.salomonbrys.kotson.set
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.Serializable
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

class OptConfig : Serializable {

    val options = ConcurrentHashMap<String, Any>()

    operator fun <T : Any> set(param: OptimizableParameter<T>, v: T) {
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

class OptConfigTypeAdaptor : JsonSerializer<OptConfig>, JsonDeserializer<OptConfig> {
    override fun serialize(src: OptConfig, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val optionsObject = JsonObject()
        for ((label, value) in src.options) {
            val valueObject = JsonObject()
            valueObject["type"] = value::class.qualifiedName
            valueObject["value"] = context.serialize(value)
            optionsObject[label] = valueObject
        }
        return optionsObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OptConfig {
        val optConfig = OptConfig()
        if (json is JsonObject) {
            for ((label, valueObject) in json.entrySet()) {
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
        } else {
            error("Expecting a JsonObject, got a ${json::class}")
        }
        return optConfig
    }

}