package club.kosya.duraexec;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class TranscribeVideoWorkflow {
    @SneakyThrows
    public String run(String videoFile) {
        log.info("run(videoFile={})", videoFile);

        var videoPath = Paths.get(System.getProperty("user.dir")).resolve(videoFile);
        if (!Files.exists(videoPath)) {
            throw new IllegalArgumentException("Video file does not exist: " + videoFile);
        }

        var ctx = new ExecutionContextImpl("1");
        Path audioFile = extractAudio(videoPath, ctx);
        try {
            return transcribeAudio(audioFile, ctx);
        } finally {
            Files.deleteIfExists(audioFile);
        }
    }

    private Path extractAudio(Path videoFile, ExecutionContext ctx) {
        String videoFileName = videoFile.getFileName().toString();
        String audioFileName = videoFileName.replaceFirst("\\.[^.]+$", ".wav");
        Path audioFile = videoFile.getParent().resolve(audioFileName);

        ExecutionResult result = ctx.executeProcess("ffmpeg",
                "-i", videoFile.toString(),
                "-vn", "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
                "-y", audioFile.toString());

        if (!result.isSuccess()) {
            throw new RuntimeException("FFmpeg failed with exit code: " + result.getExitCode());
        }

        return audioFile;
    }

    private String transcribeAudio(Path audioFile, ExecutionContext ctx) {
        ExecutionResult result = ctx.executeProcess(
                Paths.get(System.getProperty("user.dir"), "whisper").toFile(),
                "uv", "run", "whisper", audioFile.toString(), "--model", "base");

        if (!result.isSuccess()) {
            throw new RuntimeException("Whisper transcription failed with exit code: " + result.getExitCode());
        }

        return result.getOutput();
    }
}
