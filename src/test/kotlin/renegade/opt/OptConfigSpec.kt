package renegade.opt

import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

class OptConfigSpec : FreeSpec() {
    init {
        "Test Gson Serialization" - {
            val gson = GsonBuilder().registerTypeAdapter(OptConfig::class.java, OptConfigTypeAdaptor()).create()
            val optConfig = OptConfig()
            val p1 = DoubleRangeParameter("p1", 0.0 to 1.0, 0.0)
            val p2 = ValueListParameter("p2", C1.values().toList())
            optConfig[p1] = 1.5
            optConfig[p2] = C1.A
            "serialize" {
                val serialized = gson.toJson(optConfig)
                val deserialized = gson.fromJson(serialized, OptConfig::class.java)
                deserialized.options.size shouldBe 2
                deserialized[p1] shouldBe 1.5
                deserialized[p2] shouldBe C1.A
            }
        }
    }
}

enum class C1 {
    A, B
}