package com.example.taskqueueservice.service

import com.example.taskqueueservice.helper.TestDataFactory
import com.example.taskqueueservice.model.TaskStatus
import com.example.taskqueueservice.repository.TaskRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class SchedulerServiceTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var taskEventService: TaskEventService
    private lateinit var taskQueueManager: TaskQueueManager
    private lateinit var schedulerService: SchedulerService

    @BeforeEach
    fun setUp() {
        taskRepository = mockk()
        taskEventService = mockk()
        taskQueueManager = mockk()
        schedulerService = SchedulerService(taskRepository, taskEventService, taskQueueManager, 10)
    }

    @Test
    @DisplayName("Должен находить и перезапускать зависшие задачи")
    fun shouldFindAndRestartStuckTasks() {
        val stuckTask = TestDataFactory.createTask(
            status = TaskStatus.PROCESSING,
            updatedAt = Instant.now().minus(20, ChronoUnit.MINUTES)
        )

        every { taskRepository.findStuckTasks(TaskStatus.PROCESSING, any()) } returns listOf(stuckTask)
        every { taskRepository.save(any()) } returns stuckTask
        every { taskEventService.logEvent(any(), any(), any(), any(), any()) } returns mockk()
        every { taskQueueManager.submitTask(any()) } just runs

        schedulerService.checkStuckTasks()

        verify(exactly = 2) { taskRepository.save(any()) }
        verify(exactly = 1) { taskEventService.logEvent(any(), eq(com.example.taskqueueservice.model.EventType.TASK_STUCK), any(), any(), any()) }
        verify(exactly = 1) { taskQueueManager.submitTask(any()) }
    }

    @Test
    @DisplayName("Не должен трогать задачи которые не зависли")
    fun shouldNotTouchNonStuckTasks() {
        every { taskRepository.findStuckTasks(TaskStatus.PROCESSING, any()) } returns emptyList()

        schedulerService.checkStuckTasks()

        verify(exactly = 0) { taskRepository.save(any()) }
        verify(exactly = 0) { taskQueueManager.submitTask(any()) }
    }
}