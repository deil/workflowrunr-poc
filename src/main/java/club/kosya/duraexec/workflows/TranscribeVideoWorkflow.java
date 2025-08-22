package club.kosya.duraexec.workflows;

import club.kosya.duraexec.ExecutionContext;
import club.kosya.duraexec.ExecutionResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static club.kosya.duraexec.internal.ExecutionContextImpl.executeProcess;

@Slf4j
public class TranscribeVideoWorkflow {
    @SneakyThrows
    public String run(ExecutionContext ctx, String videoFile) {
        log.info("run(ctx={}, videoFile={})", ctx, videoFile);

        var videoPath = Paths.get(System.getProperty("user.dir")).resolve(videoFile);
        if (!Files.exists(videoPath)) {
            throw new IllegalArgumentException("Video file does not exist: " + videoFile);
        }

        var audioFile = ctx.action("Extract audio track", (_ctx) -> extractAudio(videoPath));
        try {
            return ctx.action("Transcribe audio to text", (_ctx) -> transcribeAudio(audioFile));
        } finally {
            Files.deleteIfExists(audioFile);
        }
    }

    private Path extractAudio(Path videoFile) {
        String videoFileName = videoFile.getFileName().toString();
        String audioFileName = videoFileName.replaceFirst("\\.[^.]+$", ".wav");
        Path audioFile = videoFile.getParent().resolve(audioFileName);

        ExecutionResult result = executeProcess("ffmpeg",
                "-i", videoFile.toString(),
                "-vn", "-acodec", "pcm_s16le", "-ar", "16000", "-ac", "1",
                "-y", audioFile.toString());

        if (!result.isSuccess()) {
            throw new RuntimeException("FFmpeg failed with exit code: " + result.getExitCode());
        }

        return audioFile;
    }

    private String transcribeAudio(Path audioFile) {
        ExecutionResult result = executeProcess(
                Paths.get(System.getProperty("user.dir"), "whisper").toFile(),
                "uv", "run", "whisper", audioFile.toString(), "--model", "base");

        if (!result.isSuccess()) {
            throw new RuntimeException("Whisper transcription failed with exit code: " + result.getExitCode());
        }

        return result.getOutput();
    }
}
