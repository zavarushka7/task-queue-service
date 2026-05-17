package com.example.taskqueueservice.service

import com.example.taskqueueservice.model.EventType
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.repository.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class SchedulerService(
    private val taskRepository: TaskRepository,
    private val taskEventService: TaskEventService,
    private val taskQueueManager: TaskQueueManager,
    @Value("\${app.processing.stuck-timeout-minutes:10}")
    private val stuckTimeoutMinutes: Long
) {

    private val logger = LoggerFactory.getLogger(SchedulerService::class.java)

    @Scheduled(fixedDelayString = "\${app.processing.stuck-check-interval-ms:300000}")
    fun checkStuckTasks() {
        logger.debug("Проверка зависших задач...")
        val timeout = Instant.now().minus(stuckTimeoutMinutes, ChronoUnit.MINUTES)

        val stuckTasks = taskRepository.findStuckTasks(TaskStatus.PROCESSING, timeout)

        stuckTasks.forEach { task ->
            task.status = TaskStatus.STUCK
            taskRepository.save(task)

            taskEventService.logEvent(
                taskId = task.id!!,
                eventType = EventType.TASK_STUCK,
                oldStatus = TaskStatus.PROCESSING,
                newStatus = TaskStatus.STUCK,
                details = "Задача зависла. Не обновлялась с ${task.updatedAt}"
            )

            task.status = TaskStatus.PENDING
            taskRepository.save(task)
            taskQueueManager.submitTask(task)

            logger.warn("Задача ${task.id} помечена как зависшая и возвращена в очередь")
        }

        if (stuckTasks.isNotEmpty()) {
            logger.info("Найдено и перезапущено зависших задач: ${stuckTasks.size}")
        }
    }
}