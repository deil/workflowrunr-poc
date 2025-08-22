package club.kosya.duraexec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecutionContext {
    public final static ExecutionContext Placeholder = new ExecutionContext(null);

    @Getter
    private final String id;
}
