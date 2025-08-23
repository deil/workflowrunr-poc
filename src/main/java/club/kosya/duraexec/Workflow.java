package club.kosya.duraexec;

import club.kosya.duraexec.internal.Execution;
import club.kosya.duraexec.internal.ExecutionStatus;
import club.kosya.duraexec.internal.ExecutionsRepository;
import club.kosya.lib.lambda.WorkflowLambda;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static club.kosya.lib.lambda.LambdaSerializer.serialize;

@RequiredArgsConstructor
@Component
public class Workflow {
    private final ExecutionsRepository executions;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Transactional
    public long run(WorkflowLambda workflow) {
        var task = new Execution();
        task.setStatus(ExecutionStatus.Queued);
        task.setQueuedAt(LocalDateTime.now());

        var serializedData = serialize(workflow);
        task.setDefinition(serializedData.definition());
        var paramsJson = objectMapper.writeValueAsString(serializedData.capturedArgs());
        task.setParams(paramsJson);

        return executions.save(task).getId();
    }
}
