package club.kosya.duraexec;

import club.kosya.duraexec.spring.DummyController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PlayDuraexecApplication implements ApplicationRunner {
    @Autowired
    DummyController controller;

    public static void main(String[] args) {
        SpringApplication.run(PlayDuraexecApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    }
}
