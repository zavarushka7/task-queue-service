package com.example.taskqueueservice.service

import com.example.taskqueueservice.model.Task
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Semaphore

@Service
class TaskQueueManager(
    private val taskProcessor: TaskProcessor,
    private val taskRepository: TaskRepository,
    private val coroutineScope: CoroutineScope,
    private val semaphore: Semaphore
) {

    private val logger = LoggerFactory.getLogger(TaskQueueManager::class.java)

    fun submitTask(task: Task) {
        coroutineScope.launch {
            try {
                semaphore.acquire()
                processTask(task)
            } catch (e: Exception) {
                logger.error("Ошибка в очереди задач", e)
            } finally {
                semaphore.release()
            }
        }
    }

    private suspend fun processTask(task: Task) {
        val freshTask = taskRepository.findById(task.id!!).orElse(null) ?: return

        if (freshTask.status != TaskStatus.PENDING) {
            return
        }

        taskProcessor.process(freshTask)
    }

    fun getActiveWorkersCount(): Int {
        val available = semaphore.availablePermits()
        val max = semaphore.drainPermits()
        semaphore.release(max)
        return max - available
    }
}