package club.kosya.duraexec;

import java.io.File;

public interface ExecutionContext {
    String getId();

    ExecutionResult executeProcess(String executable, String... args);
    ExecutionResult executeProcess(File workingDirectory, String executable, String... args);
}
