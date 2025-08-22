package club.kosya.duraexec;

import club.kosya.duraexec.internal.WorkflowAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ExecutionContext {
    public final static ExecutionContext Placeholder = new ExecutionContext(null);

    @Getter
    private final String id;

    public <R> R action(String name, Function<ExecutionContext, R> lambda) {
        var id = generateActionId(name);
        var action = new WorkflowAction(this, id, name);
        return action.execute(lambda);
    }

    private String generateActionId(String name) {
        return UUID.randomUUID().toString();
    }
}
