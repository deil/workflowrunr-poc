package club.kosya.duraexec;

import club.kosya.duraexec.internal.ExecutedAction;
import club.kosya.duraexec.internal.ExecutionFlow;
import club.kosya.duraexec.internal.ExecutionsRepository;
import club.kosya.duraexec.internal.WorkflowAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
public class ExecutionContext {
    public final static ExecutionContext Placeholder = new ExecutionContext(null, null, null);

    private final ObjectMapper objectMapper;
    private final ExecutionsRepository executions;
    private final ExecutionFlow flow;

    public ExecutionContext(String id, ObjectMapper objectMapper, ExecutionsRepository executions) {
        this.objectMapper = objectMapper;
        this.executions = executions;

        if (id == null) {
            flow = null;
        } else {
            flow = new ExecutionFlow(id);
        }
    }

    @SneakyThrows
    public <R> R action(String name, Supplier<R> lambda) {
        var tracking = new ExecutedAction(generateActionId(name));

        var action = new WorkflowAction(this, tracking.getId(), name);
        var result = action.execute(lambda::get);
        flow.getActions().add(tracking);

        val execution = executions.findById(Long.parseLong(flow.getId())).get();
        execution.setState(objectMapper.writeValueAsString(flow));
        executions.save(execution);

        return result;
    }

    private String generateActionId(String name) {
        return UUID.randomUUID().toString();
    }
}
