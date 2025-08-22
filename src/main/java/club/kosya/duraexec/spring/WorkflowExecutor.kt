package club.kosya.duraexec.spring

import club.kosya.duraexec.ExecutionContext
import club.kosya.duraexec.internal.ExecutionsRepository
import club.kosya.lib.lambda.LambdaDeserializer.deserialize
import club.kosya.lib.lambda.WorkflowLambda
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class WorkflowExecutor(
    private val executions: ExecutionsRepository,
) {
    @JvmRecord
    data class NewWorkflowSubmitted(
        val id: Long,
    )

    @Async
    @TransactionalEventListener
    fun onNewWorkflowSubmitted(event: NewWorkflowSubmitted) {
        try {
            val execution = executions.findById(event.id).get()
            val ctx = ExecutionContext(execution.id.toString())
            val lambda = deserialize<WorkflowLambda>(execution.wf, ctx, execution.param1)
            lambda.run()
        } catch (ex: Exception) {
            log.error("Oops", ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(WorkflowExecutor::class.java)
    }
}
