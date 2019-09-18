package renegade.util

import com.google.gson.*
import java.lang.reflect.Type
import kotlin.reflect.KClass

class KClassTypeAdaptor : JsonSerializer<KClass<*>>, JsonDeserializer<KClass<*>> {
    override fun serialize(src: KClass<*>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.qualifiedName)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): KClass<*> {
        return when(json.asString) {
            "kotlin.Double" -> Double::class
            "kotlin.Int" -> Int::class
            else -> Class.forName(json.asString).kotlin
        }
    }

}