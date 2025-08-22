package club.kosya.duraexec.spring;

import club.kosya.duraexec.ExecutionContext;
import club.kosya.duraexec.internal.ExecutionsRepository;
import club.kosya.lib.lambda.WorkflowLambda;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static club.kosya.lib.lambda.LambdaDeserializer.bytesToSerializedLambda;
import static club.kosya.lib.lambda.LambdaDeserializer.fromSerializedLambda;

@Slf4j
@RequiredArgsConstructor
@Component
public class WorkflowExecutor {
    private final ExecutionsRepository executions;

    public record NewWorkflowSubmitted(long id) {
    }

    @Async
    @TransactionalEventListener
    void onNewWorkflowSubmitted(NewWorkflowSubmitted event) {
        try {
            var execution = executions.findById(event.id()).get();
            var toExecute = bytesToSerializedLambda(execution.getWf());

            var ctx = new ExecutionContext(Long.toString(execution.getId()));

            WorkflowLambda restored = fromSerializedLambda(toExecute, ctx, execution.getParam1());
            restored.run();
        } catch (Exception ex) {
            log.error("Oops", ex);
        }
    }
}
