package club.kosya.duraexec.spring

import club.kosya.duraexec.ExecutionContext
import club.kosya.duraexec.internal.ExecutionStatus
import club.kosya.duraexec.internal.ExecutionsRepository
import club.kosya.lib.lambda.LambdaDeserializer.deserialize
import club.kosya.lib.lambda.WorkflowLambda
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import java.time.LocalDateTime

@Component
class WorkflowExecutor(
    private val executions: ExecutionsRepository,
    private val objectMapper: ObjectMapper,
) {
    @JvmRecord
    data class NewWorkflowSubmitted(
        val id: Long,
    )

    @Async
    @TransactionalEventListener
    fun onNewWorkflowSubmitted(event: NewWorkflowSubmitted) {
        try {
            var execution = executions.findById(event.id).get()
            val ctx = ExecutionContext(execution.id.toString(), objectMapper, executions)

            val args = mutableListOf<Any>(ctx)
            val params = objectMapper.readValue(execution.params, ArrayList::class.java) as List<Any>
            args.addAll(params)

            val lambda = deserialize<WorkflowLambda>(execution.definition, args)
            execution.status = ExecutionStatus.Running
            execution.startedAt = LocalDateTime.now()
            executions.save(execution)

            lambda.run()

            execution = executions.findById(event.id).get()
            execution.status = ExecutionStatus.Completed
            execution.completedAt = LocalDateTime.now()
            executions.save(execution)
        } catch (ex: Exception) {
            log.error("Oops", ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(WorkflowExecutor::class.java)
    }
}
