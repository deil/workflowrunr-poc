package club.kosya.duraexec.spring

import club.kosya.duraexec.ExecutionContext
import club.kosya.duraexec.internal.ExecutionsRepository
import club.kosya.lib.lambda.LambdaDeserializer
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
            val toExecute = LambdaDeserializer.bytesToSerializedLambda(execution.wf)

            val ctx = ExecutionContext(execution.id.toString())

            val lambda =
                LambdaDeserializer.fromSerializedLambda<WorkflowLambda?>(toExecute, ctx, execution.param1)
            lambda.run()
        } catch (ex: Exception) {
            log.error("Oops", ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(WorkflowExecutor::class.java)
    }
}
