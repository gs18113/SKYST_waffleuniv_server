package com.example.skystWaffleunivServer.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@Configuration
@EnableScheduling
class SchedulingConfig {
    @Bean
    fun songPlayerExecutor(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(5)
    }
}
