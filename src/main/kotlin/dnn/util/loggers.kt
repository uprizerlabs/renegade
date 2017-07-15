package dnn.util

import org.slf4j.*
import java.text.NumberFormat

/**
 * Created by ian on 7/9/17.
 */

private val numberFormat = NumberFormat.getInstance()

fun <R> Logger.mdc(vararg map: Pair<String, Any>, run : () -> R) : R {
    for ((k, v) in map) {
        MDC.put(k, v.toString())
    }
    val r = run()
    for ((k, _) in map) {
        MDC.remove(k)
    }
    return r
}

sealed class TaskResult<out ResultType>(open val result : ResultType) {
    data class ResultWithDescription<out ResultType>(
            override val result : ResultType,
            val description : String) : TaskResult<ResultType>(result)
    data class Result<out ResultType>(override val result : ResultType) : TaskResult<ResultType>(result)
    class NoResult : TaskResult<Unit>(Unit)
}
