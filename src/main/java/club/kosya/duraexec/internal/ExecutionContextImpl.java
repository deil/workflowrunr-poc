package club.kosya.duraexec.internal;

import club.kosya.duraexec.ExecutionResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;

@RequiredArgsConstructor
public class ExecutionContextImpl {
    @Getter
    private final String id;

    @SneakyThrows
    public static ExecutionResult executeProcess(String executable, String... args) {
        return executeProcess(null, executable, args);
    }

    @SneakyThrows
    public static ExecutionResult executeProcess(File workingDirectory, String executable, String... args) {
        String[] command = new String[args.length + 1];
        command[0] = executable;
        System.arraycopy(args, 0, command, 1, args.length);

        ProcessBuilder pb = new ProcessBuilder(command);
        if (workingDirectory != null) {
            pb.directory(workingDirectory);
        }

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (var reader = process.inputReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        return new ExecutionResult(exitCode, output.toString().trim());
    }
}
