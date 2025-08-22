package club.kosya.duraexec.internal

import club.kosya.duraexec.ExecutionContext
import org.slf4j.LoggerFactory
import java.util.function.Function

class WorkflowAction(
    private val ctx: ExecutionContext,
    private val id: String,
    private val name: String,
) {
    fun <R> execute(lambda: Function<ExecutionContext, R>): R {
        log.info("[{}] Action: {}", id, name)
        try {
            val result: R = lambda.apply(ctx)
            log.info("[{}] Action result: {}", id, result)

            return result
        } catch (ex: Exception) {
            log.error("Exception while executing action: {}", ex.message, ex)
            throw RuntimeException(ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(WorkflowAction::class.java)
    }
}
