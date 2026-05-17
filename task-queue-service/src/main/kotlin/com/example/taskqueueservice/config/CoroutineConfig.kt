package com.example.taskqueueservice.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Semaphore

@Configuration
class CoroutineConfig {

    @Value("\${app.queue.max-concurrent-tasks:5}")
    private var maxConcurrentTasks: Int = 5

    @Bean
    fun taskProcessingScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }

    @Bean
    fun taskSemaphore(): Semaphore {
        return Semaphore(maxConcurrentTasks)
    }
}