package dnn.util

import dnn.util.TaskResult.ResultWithDescription
import org.slf4j.Logger
import java.text.NumberFormat

/**
 * Created by ian on 7/9/17.
 */

private val numberFormat = NumberFormat.getInstance()

fun <R> Logger.infoTask(description : String, block : () -> TaskResult<R>) : R {
    this.info("Starting $description:")
    val startTimeMS = System.currentTimeMillis()
    val result = block()
    val durationMS = System.currentTimeMillis() - startTimeMS
    val resultDescription = when (result) {
        is ResultWithDescription<R> -> ", Result: ${result.description}"
        else -> ""
    }
    this.info("Finished $description in ${numberFormat.format(durationMS)}ms"+resultDescription)
    return result.result
}

sealed class TaskResult<out ResultType>(open val result : ResultType) {
    data class ResultWithDescription<out ResultType>(
            override val result : ResultType,
            val description : String) : TaskResult<ResultType>(result)
    data class Result<out ResultType>(override val result : ResultType) : TaskResult<ResultType>(result)
    class NoResult : TaskResult<Unit>(Unit)
}
