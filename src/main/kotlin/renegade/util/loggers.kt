package renegade.util

import java.text.NumberFormat

/**
 * Created by ian on 7/9/17.
 */

sealed class TaskResult<out ResultType>(open val result : ResultType) {
    data class ResultWithDescription<out ResultType>(
            override val result : ResultType,
            val description : String) : TaskResult<ResultType>(result)
    data class Result<out ResultType>(override val result : ResultType) : TaskResult<ResultType>(result)
    class NoResult : TaskResult<Unit>(Unit)
}
