package club.kosya.duraexec.spring;

import club.kosya.duraexec.ExecutionContext;
import club.kosya.duraexec.Workflow;
import club.kosya.duraexec.workflows.TranscribeVideoWorkflow;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DummyController {
    private final Workflow workflow;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @PostMapping
    public String runExampleWorkflow(@RequestBody RunExampleWorkflowRequest body) {
        // can't use body directly, as DummyRequest will become captured parameter
        var fileName = body.file();

        // can't use in lambda directly, because in that case it won't be captured as parameter
        var ctx = ExecutionContext.Placeholder;

        var executionId = workflow.run(() -> new TranscribeVideoWorkflow().run(ctx, fileName), fileName);
        eventPublisher.publishEvent(new WorkflowExecutor.NewWorkflowSubmitted(executionId));

        return Long.toString(executionId);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RunExampleWorkflowRequest(String file) {
    }
}
