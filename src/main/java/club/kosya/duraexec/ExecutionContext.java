package club.kosya.duraexec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
public class ExecutionContext {
    public final static ExecutionContext Placeholder = new ExecutionContext(null);

    @Getter
    private final String id;

    public <R> R action(String name, Callable<R> action) {
        log.info("Action: {}", name);
        try {
            var result = action.call();
            log.info("Action result: {}", result);

            return result;
        } catch (Exception ex) {
            log.error("Exception while executing action: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
}
