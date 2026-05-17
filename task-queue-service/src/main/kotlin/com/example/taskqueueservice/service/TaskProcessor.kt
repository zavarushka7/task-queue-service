package com.example.taskqueueservice.service

import com.example.taskqueueservice.exception.UnsupportedTaskTypeException
import com.example.taskqueueservice.model.EventType
import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.repository.TaskRepository
import com.example.taskqueueservice.service.strategy.TaskProcessingStrategy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TaskProcessor(
    private val taskRepository: TaskRepository,
    private val taskEventService: TaskEventService,
    private val strategies: List<TaskProcessingStrategy>,
    @Value("\${app.processing.max-retry-attempts:3}")
    private val maxRetryAttempts: Int
) {

    private val logger = LoggerFactory.getLogger(TaskProcessor::class.java)

    @Transactional
    suspend fun process(task: Task) {
        val startTime = System.currentTimeMillis()

        try {
            task.status = TaskStatus.PROCESSING
            task.startedAt = Instant.now()
            taskRepository.save(task)

            taskEventService.logEvent(
                taskId = task.id!!,
                eventType = EventType.PROCESSING_STARTED,
                oldStatus = TaskStatus.PENDING,
                newStatus = TaskStatus.PROCESSING,
                details = "Начата обработка файла: ${task.originalFileName}"
            )

            val strategy = strategies.firstOrNull { it.supports(task.type) }
                ?: throw UnsupportedTaskTypeException(task.type)

            val result = strategy.process(task)

            val processingTime = System.currentTimeMillis() - startTime
            task.status = TaskStatus.COMPLETED
            task.result = result
            task.completedAt = Instant.now()
            task.processingTimeMs = processingTime
            taskRepository.save(task)

            taskEventService.logEvent(
                taskId = task.id!!,
                eventType = EventType.TASK_COMPLETED,
                oldStatus = TaskStatus.PROCESSING,
                newStatus = TaskStatus.COMPLETED,
                details = "Обработка завершена за ${processingTime}мс. Результат: $result"
            )

        } catch (e: Exception) {
            logger.error("Ошибка обработки задачи ${task.id}", e)
            handleError(task, e)
        }
    }

    private suspend fun handleError(task: Task, exception: Exception) {
        task.retryCount++
        task.errorMessage = exception.message

        if (task.retryCount < maxRetryAttempts) {
            task.status = TaskStatus.PENDING
            taskRepository.save(task)

            taskEventService.logEvent(
                taskId = task.id!!,
                eventType = EventType.RETRY_SCHEDULED,
                details = "Попытка ${task.retryCount}/$maxRetryAttempts. Ошибка: ${exception.message}"
            )
        } else {
            task.status = TaskStatus.FAILED
            task.completedAt = Instant.now()
            taskRepository.save(task)

            taskEventService.logEvent(
                taskId = task.id!!,
                eventType = EventType.TASK_FAILED,
                oldStatus = TaskStatus.PROCESSING,
                newStatus = TaskStatus.FAILED,
                details = "Задача провалена после $maxRetryAttempts попыток. Ошибка: ${exception.message}"
            )
        }
    }
}