package club.kosya.duraexec.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class BeansConfig {
    @Bean
    ExecutorService taskExecutor() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
