package club.kosya.duraexec.spring;

import club.kosya.duraexec.ExecutionContext;
import club.kosya.duraexec.internal.Execution;
import club.kosya.duraexec.internal.ExecutionsRepository;
import club.kosya.duraexec.workflows.TranscribeVideoWorkflow;
import club.kosya.lib.lambda.WorkflowLambda;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static club.kosya.lib.lambda.LambdaSerializer.serialize;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DummyController {
    private final ExecutionsRepository executions;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @PostMapping
    public String dummy(@RequestBody DummyRequest body) {
        // can't use body directly, as DummyRequest will become captured parameter
        var fileName = body.file();

        // can't use in lambda directly, because in that case it won't be captured as parameter
        var ctx = ExecutionContext.Placeholder;
        WorkflowLambda wf = () -> new TranscribeVideoWorkflow().run(ctx, fileName);

        var task = new Execution();
        task.setWf(serialize(wf));
        task.setParam1(body.file());
        task = executions.save(task);

        var taskId = task.getId();
        eventPublisher.publishEvent(new WorkflowExecutor.NewWorkflowSubmitted(taskId));

        return Long.toString(task.getId());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DummyRequest(String file) {
    }
}
