package renegade.opt

import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import io.kotlintest.matchers.plusOrMinus
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

class OptConfigSpec : FreeSpec() {
    init {
        "Test Gson Serialization" - {
            val gson = Optimizer.gson
            val optConfig = OptConfig()
            val p1 = DoubleRangeParameter("p1", 0.0 to 1.0, 0.0)
            val p2 = ValueListParameter("p2", C1.values().toList())
            optConfig[p1] = 1.5
            optConfig[p2] = C1.A
            "serialize" {
                optConfig.parameters?.get("p1") shouldBe p1
                optConfig.parameters?.get("p2") shouldBe p2

                val serialized = gson.toJson(optConfig)
                val deserialized = gson.fromJson(serialized, OptConfig::class.java)
                deserialized.options.size shouldBe 2.0.plusOrMinus(0.0000001)
                deserialized[p1] shouldBe 1.5.plusOrMinus(0.0000001)
                deserialized[p2] shouldBe C1.A
                deserialized.parameters?.get("p1") shouldBe p1
                deserialized.parameters?.get("p2") shouldBe p2
            }
        }
    }
}

enum class C1 {
    A, B
}