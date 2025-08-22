package club.kosya.duraexec.spring

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class BeansConfig {
    @Bean
    fun taskExecutor(): ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
}
